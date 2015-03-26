<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
// current auctions page


global $config;
// need to log in
if(!$config['user']->isOk()) {
  ForwardTo('./', 0);
  exit();
}


// need to change temp pass
if($config['user']->isTempPass()) {
  ForwardTo('./?page=changepass', 0);
  exit();
}


// locked inventory
if($config['user']->isLocked()) {
  $_SESSION['error'][] = 'Your inventory is currently locked.<br />Please close your in game inventory and try again.';
} else {

  // fixed price
  if($config['action']=='fixedprice') {
    CSRF::ValidateToken();
    if(AuctionFuncs::SellFixed(
      getVar('id'   ,'int'   ,'post'),
      getVar('qty'  ,'int'   ,'post'),
      getVar('priceFixed','double','post'),
      getVar('desc' ,'string','post')
    )){
      $_SESSION['success'][] = 'Auction created successfully!';
      ForwardTo(getLastPage(), 0);
      exit();
    }
  } else

  // auction
  if($config['action']=='auction') {
    //TODO:
    echo 'Sorry, this feature isn\'t ready yet';
    exit();
  } else

  // server shop
  if($config['action']=='servershop') {
    CSRF::ValidateToken();
    if(ServerShopFuncs::CreateShop(
      getVar('id'   ,'int'   ,'post'),
      getVar('qty'  ,'int'   ,'post'),
      getVar('priceBuy',  'double', 'post'),
      getVar('priceSell', 'double', 'post')
    )){
      $_SESSION['success'][] = 'Server Shop created successfully!';
      ForwardTo(getLastPage(), 0);
      exit();
    }
  }

}


////	$minBid = mysql_real_escape_string(stripslashes(round($_POST['MinBid'], 2)));
//$minBid=0;
//	$allowBids = 1;
//	if (mysql_real_escape_string(stripslashes($_POST['MinBid'])) == ""){
//		$allowBids = 0;
//	}

//					if ($isAdmin){						
//						if ($chargeAdmins){
//							$itemFee = (($item->marketprice/100)*$auctionFee)*$sellQuantity;
//						}else{
//							$itemFee = 0;
//						}
//						if ($player->money >= $itemFee){
//							$item->changeQuantity(0 - $sellQuantity);
//							$player->spend($itemFee, $useMySQLiConomy, $iConTableName);
//$itemQuery = mysql_query("INSERT INTO WA_Auctions (name, damage, player, quantity, price, created, allowBids, currentBid, currentWinner) ".
//                         "VALUES ('$item->name', '$item->damage', '$item->owner', '$sellQuantity', '$sellPrice', NOW(), '$allowBids', '$minBid', '$item->owner')");
//							$queryLatestAuction = mysql_query("SELECT id FROM WA_Auctions ORDER BY id DESC");
//							list($latestId)= mysql_fetch_row($queryLatestAuction);
//							if ($item->quantity == 0)
//							{
//								$item->delete();
//							}
//							if ($useTwitter == true){
//								try{
//								$twitter = new Twitter($consumerKey, $consumerSecret, $accessToken, $accessTokenSecret);
//								if ($sellQuantity == 0){
//									$twitQuant = "Infinite";
//								}else{
//									$twitQuant = $sellQuantity;
//								}
//								$twitter->send('[WA] Auction Created: '.$user.' is selling '.$twitQuant.' x '.$itemFullName.' for '.$currencyPrefix.$sellPrice.$currencyPostfix.' each. At '.date("H:i:s").' #webauction');
//								}catch (Exception $e){
//									//normally means you reached the daily twitter limit.
//								}
//							}
//							$queryEnchants=mysql_query("SELECT * FROM WA_EnchantLinks WHERE itemId='$item->id' AND itemTableId ='0'"); 
//							while(list($idk,$enchIdk, $tableIdk, $itemIdk)= mysql_fetch_row($queryEnchants))
//							{ 
//								$updateEnch = mysql_query("INSERT INTO WA_EnchantLinks (enchId, itemTableId, itemId) VALUES ('$enchIdk', '1', '$latestId')");
//							}
//						
//							$_SESSION['success'] = "You auctioned $sellQuantity $itemFullName for ".$currencyPrefix.$sellPrice.$currencyPostfix." each, the fee was ".$currencyPrefix.$itemFee.$currencyPostfix;
//							header("Location: ../myauctions.php");
//						}else
//						{
//						$_SESSION['error'] = 'Fee cost '.$currencyPrefix.$itemFee.$currencyPostfix.', you did not have enough money.';
//						header("Location: ../myauctions.php");
//						}
//					}else{
//						if ($sellQuantity > 0){							
//							$itemFee = (($item->marketprice/100)*$auctionFee)*$sellQuantity;
//							if ($player->money >= $itemFee){
//								$item->changeQuantity(0 - $sellQuantity);
//								$player->spend($itemFee, $useMySQLiConomy, $iConTableName);
//$itemQuery = mysql_query("INSERT INTO WA_Auctions (name, damage, player, quantity, price, created, allowBids, currentBid, currentWinner) ".
//                         "VALUES ('$item->name', '$item->damage', '$item->owner', '$sellQuantity', '$sellPrice', NOW(), '$allowBids', '$minBid', '$item->owner')");
//								$queryLatestAuction = mysql_query("SELECT id FROM WA_Auctions ORDER BY id DESC");
//								list($latestId)= mysql_fetch_row($queryLatestAuction);
//								if ($item->quantity == 0)
//								{
//									$item->delete();
//								}
//								if ($useTwitter == true){
//									try{
//									$twitter = new Twitter($consumerKey, $consumerSecret, $accessToken, $accessTokenSecret);
//									$twitter->send('[WA] Auction Created: '.$user.' is selling '.$sellQuantity.' x '.$itemFullName.' for '.$currencyPrefix.$sellPrice.$currencyPostfix.' each. At '.date("H:i:s").'. '.$shortLinkToAuction.' #webauction');
//									}catch (Exception $e){
//										//normally means you reached the daily twitter limit.
//									}
//								}
//								$queryEnchants=mysql_query("SELECT * FROM WA_EnchantLinks WHERE itemId='$item->id' AND itemTableId ='0'"); 
//								while(list($idk,$enchIdk, $tableIdk, $itemIdk)= mysql_fetch_row($queryEnchants))
//								{ 
//									$updateEnch = mysql_query("INSERT INTO WA_EnchantLinks (enchId, itemTableId, itemId) VALUES ('$enchIdk', '1', '$latestId')");
//								}
//						
//								$_SESSION['success'] = "You auctioned $sellQuantity $itemFullName for ".$currencyPrefix.$sellPrice.$currencyPostfix." each, the fee was ".$currencyPrefix.$itemFee.$currencyPostfix;
//								header("Location: ../myauctions.php");
//							}else
//							{
//							$_SESSION['error'] = 'Fee cost '.$currencyPrefix.$itemFee.$currencyPostfix.', you did not have enough money.';
//							header("Location: ../myauctions.php");
//							}
//						}else
//						{
//							$_SESSION['error'] = 'Quantity was not an integer.';
//							header("Location: ../myauctions.php");
//						}
//					}
//				}else
//				{
//				    $_SESSION['error'] = 'You do not have enough of that item.';
//					header("Location: ../myauctions.php");
//				}
//			}else
//			{
//				$_SESSION['error'] = 'Quantity was not an integer.';
//				header("Location: ../myauctions.php");
//			}
//		}else
//		{
//			$_SESSION['error'] = 'Price was not an integer.';
//			header("Location: ../myauctions.php");
//		}
//	}
//	}


function RenderPage_sell(){global $config,$html,$user;
  $config['title'] = 'Sell Items';
  $id = getVar('id', 'int');
  // load page html
  $outputs = RenderHTML::LoadHTML('pages/sell.php');
  if(!is_array($outputs)) {echo 'Failed to load html!'; exit();}
  // load javascript
  $html->addToHeader($outputs['header']);
  // query item
  $Item = QueryItems::QuerySingle($user->getId(), $id);
  if(!$Item) {
    return('<h2 style="text-align: center;">The item you\'re trying to sell couldn\'t be found!</h2>');
  }
  $qty        = getVar('qty');
  $priceFixed = getVar('priceFixed', 'double');
  $priceStart = getVar('priceStart', 'double');
  $priceBuy   = getVar('priceBuy',   'double');
  $priceSell  = getVar('priceSell',  'double');
  if(empty($qty)) $qty = $Item->getItemQty();
  if($priceFixed < 0.0) $priceFixed = 0.0;
  if($priceStart < 0.0) $priceStart = 0.0;
  if($priceBuy   < 0.0) $priceBuy   = 0.0;
  if($priceSell  < 0.0) $priceSell  = 0.0;
  $messages = '';
  $tags = array(
    'messages'     => &$messages,
    'item id'      => (int) $id,
    'item display' => $Item->getDisplay(),
    'have qty'     => (int) $Item->getItemQty(),
    'qty'          => (int) $qty,
    'price fixed'  => (double) $priceFixed,
    'price start'  => (double) $priceStart,
    'price buy'    => (double) $priceBuy,
    'price sell'   => (double) $priceSell,
    'currency prefix'  => SettingsClass::getString('Currency Prefix'),
    'currency postfix' => SettingsClass::getString('Currency Postfix'),
  );
  unset($Item);
  // input errors
  if(isset($_SESSION['error'])) {
    if(is_array($_SESSION['error'])) {
      foreach($_SESSION['error'] as $msg)
        $messages .= str_replace('{message}', $msg, $outputs['error']);
    } else {
      $messages .= str_replace('{message}', $_SESSION['error'], $outputs['error']);
    }
    unset($_SESSION['error']);
  }
  if(!$user->hasPerms('canSell'))
    $messages .= str_replace('{message}', 'You don\'t have permission to sell.', $outputs['error']);
  RenderHTML::RenderTags($outputs['body'], $tags);
  unset($tags);
  return $outputs['body'];
}


?>