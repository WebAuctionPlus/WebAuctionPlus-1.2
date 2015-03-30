<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
// this class is a group of functions to handle shops
class ServerShopFuncs {


// create new server shop
public static function CreateShop($id, $qty, $priceBuy, $priceSell) {global $config, $user;
  // has isAdmin permissions
  if(!$user->hasPerms('isAdmin')) {
    $_SESSION['error'][] = 'You don\'t have permission to create a server shop.';
    return(FALSE);
  }
  // sanitize args
  $id  = (int) $id;
  $qty = (int) $qty;
  if($id  < 1) {
    $_SESSION['error'][] = 'Invalid item id!';
    return(FALSE);
  }
  if($qty < 0) {
    $_SESSION['error'][] = 'Invalid qty!';
    return(FALSE);
  }
  $priceBuy  = floor($priceBuy  * 100.0) / 100.0;
  $priceSell = floor($priceSell * 100.0) / 100.0;
  if($priceBuy  <= 0.0 && $priceSell <= 0.0) {
    $_SESSION['error'][] = 'Invalid price! Must provide either buy, sell, or both.';
    return(FALSE);
  }
  // check max price
  $maxSellPrice = SettingsClass::getDouble('Max Sell Price');
  if($maxSellPrice > 0.0 && $priceBuy  > $maxSellPrice) {
    $_SESSION['error'][] = 'Over max buy price of '.SettingsClass::getString('Currency Prefix').
      $maxSellPrice.SettingsClass::getString('Currency Postfix').' !';
    return(FALSE);
  }
  if($maxSellPrice > 0.0 && $priceSell > $maxSellPrice) {
    $_SESSION['error'][] = 'Over max sell price of '.SettingsClass::getString('Currency Prefix').
      $maxSellPrice.SettingsClass::getString('Currency Postfix').' !';
    return(FALSE);
  }
  if(!empty($desc)) {
    $desc = preg_replace('/<[^>]*>/', '', $desc);
    $desc = preg_replace('/\b(https?|ftp|file):\/\/[-A-Z0-9+&@#\/%?=~_|$!:,.;]*[A-Z0-9+&@#\/%=~_|$]/i', '', strip_tags($desc) );
  }
  // query item
  $Item = QueryItems::QuerySingle($user->getId(), $id);
  if(!$Item) {
    $_SESSION['error'][] = 'Item not found!';
    return(FALSE);
  }
  // create server shop
  $query = "INSERT INTO `".$config['table prefix']."ServerShops` (".
           "`itemId`, `itemDamage`, `itemData`, `qty`, `enchantments`, `priceBuy`, `priceSell`, `created`, `itemTitle` )VALUES( ".
           ((int) $Item->getItemId()).", ".
           ((int) $Item->getItemDamage()).", ".
           "'".mysql_san($Item->getItemData())."', ".
           ((int) $qty).", ".
           "'".mysql_san($Item->getEnchantmentsCompressed())."', ".
           ((double) $priceBuy).", ".
           ((double) $priceSell).", ".
           "NOW(), ".
           "'".mysql_san($Item->getItemTitle())."' )";
  $result = RunQuery($query, __file__, __line__);
  if(!$result) {
    echo '<p style="color: red;">Error creating server shop!</p>';
    exit();
  }
  return(TRUE);
}


// buy from server shop
public static function BuyShop($shopId, $qty) {global $config, $user;
  // has canBuy permissions
  if(!$user->hasPerms('canBuy')) {
    $_SESSION['error'][] = 'You don\'t have permission to buy.';
    return(FALSE);
  }
  // sanitize args
  $shopId = (int) $shopId;
  $qty    = (int) $qty;
  if($shopId < 1) {
    $_SESSION['error'][] = 'Invalid server shop id!';
    return(FALSE);
  }
  if($qty < 1) {
    $_SESSION['error'][] = 'Invalid qty!';
    return(FALSE);
  }
  // query shop
  $shop = QueryAuctions::QuerySingleShop($shopId);
  if(!$shop) {
    $_SESSION['error'][] = 'Shop not found!';
    return(FALSE);
  }
  $Item = $shop->getItemCopy();
  if($Item->getItemQty() > 0 && $qty > $Item->getItemQty()) {
    $_SESSION['error'][] = 'Not that many for sale!';
    return(FALSE);
  }
  // shop price
  $shopPrice = $shop->getPriceBuy();
  if($shopPrice <= 0.0) {
    $_SESSION['error'][] = 'Cannot buy from this shop!';
    return(FALSE);
  }
  $priceTotal = $shopPrice * ((double)$qty);
  if($priceTotal > $user->getMoney()) {
    $_SESSION['error'][] = 'You don\'t have enough money!';
    return(FALSE);
  }
  // make payment from buyer
  UserClass::PaymentQuery($user->getName(), $user->getUUID(), 0-$priceTotal);
  // remove shop
  if($Item->getItemQty() != 0) {
    if(!self::RemoveShop($shopId, ($qty < $Item->getItemQty() ? $qty : -1) )) {
      echo '<p style="color: red;">Error removing/updating shop!</p>';
      exit();
    }
  }
  // add to inventory
  $Item->setItemQty($qty);
  $tableRowId = ItemFuncs::AddCreateItem($user->getId(), $Item);
  if(!$tableRowId) {
    echo '<p style="color: red;">Error adding item to your inventory!</p>';
    exit();
  }
  // success
  $_SESSION['success'][] = 'Bought '.$qty.' items for '.SettingsClass::getString('Currency Prefix').
                           $priceTotal.SettingsClass::getString('Currency Postfix');
  // add sale log
  LogSales::addLog(
    LogSales::LOG_SALE,
    LogSales::SALE_SERVER,
    NULL,
    $user->getId(),
    $Item,
    $priceTotal,
    FALSE,
    '',
    FALSE
  );
  return(TRUE);
}


public static function SellShop($shopId, $qty) {global $config, $user;
  // has canSell permissions
  if(!$user->hasPerms('canSell')) {
    $_SESSION['error'][] = 'You don\'t have permission to sell.';
    return(FALSE);
  }
  // sanitize args
  $shopId = (int) $shopId;
  $qty    = (int) $qty;
  if($shopId < 1) {
    $_SESSION['error'][] = 'Invalid server shop id!';
    return(FALSE);
  }
  if($qty < 1) {
    $_SESSION['error'][] = 'Invalid qty!';
    return(FALSE);
  }
  // query shop
  $shop = QueryAuctions::QuerySingleShop($shopId);
  if(!$shop) {
    $_SESSION['error'][] = 'Shop not found!';
    return(FALSE);
  }
  $shopItem = $shop->getItem();
  if(!$shopItem) {
    $_SESSION['error'][] = 'Failed to get item info for server shop!';
    return(FALSE);
  }
  // query player items
  $Items = QueryItems::QueryInventory($user->getId(), $shopItem);
  if(!$Items) {
    $_SESSION['error'][] = 'Failed to get item from inventory!';
    return(FALSE);
  }
  // shop price
  $shopPrice = $shop->getPriceSell();
  if($shopPrice <= 0.0) {
    $_SESSION['error'][] = 'Cannot sell to this shop!';
    return(FALSE);
  }
  // sell multiple stacks
  $hasFound = FALSE;
  $soldCount = 0;
  while(TRUE) {
    $Item = $Items->getNext();
    // no more stacks found
    if(!$Item) break;
    // remove empty stack
    if($Item->getItemQty() <= 0) {
      ItemFuncs::RemoveItem($Item->getTableRowId(), -1);
      continue;
    }
    // sold enough
    if($soldCount >= $qty) break;
    $hasFound = TRUE;
    // sell partial stack
    if($qty - $soldCount < $Item->getItemQty()) {
      $sellQty = $qty - $soldCount;
      $soldCount += $sellQty;
      if(!ItemFuncs::RemoveItem($Item->getTableRowId(), $sellQty)) {
        $_SESSION['error'][] = 'Failed to remove sold item!';
        return(FALSE);
      }
    // sell full stack
    } else {
      $soldCount += $Item->getItemQty();
      if(!ItemFuncs::RemoveItem($Item->getTableRowId(), -1)) {
        $_SESSION['error'][] = 'Failed to remove sold item!';
        return(FALSE);
      }
    }
  }
  // no items sold
  if(!$hasFound || $soldCount <= 0) {
    $_SESSION['error'][] = 'You don\'t have any of this item!';
    return(FALSE);
  }
  // price for sold items
  $priceTotal = $shopPrice * ((double)$soldCount);
  // success
  $_SESSION['success'][] = 'Sold '.$soldCount.' items for '.SettingsClass::getString('Currency Prefix').
                           $priceTotal.SettingsClass::getString('Currency Postfix');
  // make payment to seller
  UserClass::PaymentQuery($user->getName(), $user->getUUID(), $priceTotal);
  // sold less than requested
  if($qty > $soldCount) {
    $_SESSION['error'][] = 'You don\'t have that many!';
  }
  // add sale log
  $Item->setItemQty($soldCount);
  LogSales::addLog(
    LogSales::LOG_SALE,
    LogSales::SALE_SERVER,
    NULL,
    $user->getId(),
    $Item,
    $priceTotal,
    FALSE,
    '',
    FALSE
  );
  return(TRUE);
}


public static function CancelShop($shopId) {global $config, $user;
  // isAdmin
  if(!$user->hasPerms('isAdmin')) {
    $_SESSION['error'][] = 'You don\'t have permission to cancel this server shop.';
    return(FALSE);
  }
  // sanitize args
  $shopId = (int) $shopId;
  if($shopId < 1) {
    $_SESSION['error'][] = 'Invalid server shop id!';
    return(FALSE);
  }
  // remove shop
  $query = "DELETE FROM `".$config['table prefix']."ServerShops` WHERE `id` = ".((int)$shopId)." LIMIT 1";
  $result = RunQuery($query, __file__, __line__);
  if(!$result || mysql_affected_rows() == 0) {
    echo '<p style="color: red;">Error removing shop!</p>';
    exit();
  }
//  // add log
//  LogSales::addLog(
//    LogSales::LOG_CANCEL,
//    LogSales::SALE_SERVER,
//    $user->getId(),
//    NULL,
//    $Item,
//    0.0,
//    FALSE,
//    ''
//  );
  return(TRUE);
}


// update qty / remove shop
protected static function RemoveShop($shopId, $qty=-1) {global $config;
  if($shopId < 1) {
    $_SESSION['error'][] = 'Invalid shop id!';
    return(FALSE);
  }
  // remove shop
  if($qty < 0) {
    $query = "DELETE FROM `".$config['table prefix']."ServerShops` WHERE `id` = ".((int)$shopId)." LIMIT 1";
    $result = RunQuery($query, __file__, __line__);
    if(!$result || mysql_affected_rows() == 0) {
      echo '<p style="color: red;">Error removing shop!</p>';
      exit();
    }
  // subtract qty
  } else {
    $query = "UPDATE `".$config['table prefix']."ServerShops` SET `qty` = `qty` - ".((int)$qty)." WHERE `id` = ".((int)$shopId)." LIMIT 1";
    $result = RunQuery($query, __file__, __line__);
    if(!$result || mysql_affected_rows() == 0) {
      echo '<p style="color: red;">Error updating shop!</p>';
      exit();
    }
    // double check qty
    $query = "SELECT `qty` FROM ".$config['table prefix']."ServerShops` WHERE `id` = ".((int)$shopId)." LIMIT 1";
    $result = RunQuery($query, __file__, __line__);
    if(!$result) {
      echo '<p style="color: red;">Error update/verify shop!</p>';
      exit();
    }
    $row = mysql_fetch_assoc($this->result_price);
    if(!$row) {
      echo '<p style="color: red;">Error update/verify shop!</p>';
      exit();
    }
    if($row['qty'] <= 0)
      self::RemoveShop($shopId, -1);
  }
  return(TRUE);
}


}
?>