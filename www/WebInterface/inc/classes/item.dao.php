<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
// item stack object
class ItemDAO{


protected $tableRowId =-1;
protected $itemId     =-1;
protected $itemDamage = 0;
protected $itemData = NULL;
protected $qty        = 0;
protected $enchantments = array();
protected $marketPrice = False;
protected $marketPriceTotal = False;


function __construct($tableRowId=0, $itemId=0, $itemDamage=0, $itemData=NULL, $qty=0, $marketPrice = "--", $marketPriceTotal = "--", $enchantments=array()){
  $this->tableRowId = ($tableRowId<1 ? -1 : (int)$tableRowId);
  $this->itemId     = ($itemId<1     ? -1 : (int)$itemId);
  $this->itemDamage = ($itemDamage<1 ?  0 : (int)$itemDamage);
  $this->itemData   = $itemData;
  $this->qty        = ($qty<1        ?  0 : (int)$qty);
  $this->marketPrice = $marketPrice;
  $this->marketPriceTotal = $marketPriceTotal;
  $this->enchantments = self::parseEnchantments($enchantments);
}


// copy item object
public function getCopy(){
  return(self::makeCopy($this));
}
public static function makeCopy($Item){
  return(new ItemDAO(
    $Item->getTableRowId(),
    $Item->getItemId(),
    $Item->getItemDamage(),
    $Item->getItemData(),
    $Item->getItemQty(),
    $Item->getMarketPrice(),
    $Item->getMarketPriceTotal(),
    $Item->getEnchantmentsArray()
  ));
}


// table row id
public function getTableRowId(){
  return((int)$this->tableRowId);
}
public function setTableRowId($rowId){
  if($rowId < 1) $rowId = -1;
  $this->tableRowId = (int)$rowId;
}
// item type
public function getItemType(){
  ItemFuncs::getItemType($this->itemId);
}
// item type id
public function getItemId(){
  return((int)$this->itemId);
}
// item damage value
public function getItemDamage(){
  return((int)$this->itemDamage);
}
// item data
public function getItemData(){
  return($this->itemData);
}
// item qty
public function getItemQty(){
  return((int)$this->qty);
}
public function setItemQty($qty){
  if($qty < 0) $qty = 0;
  $this->qty = $qty;
}
public function subtractQty($qty){
  $this->qty -= $qty;
  if($this->qty < 0) $this->qty = 0;
}


// enchantments
public function getEnchantmentsArray(){
  self::sortEnchantments($this->enchantments);
  return($this->enchantments);
}
public function getEnchantmentsCompressed(){
  self::sortEnchantments($this->enchantments);
  return(self::compressEnchantments($this->enchantments));
}
public function addEnchantment($enchId, $level){
  $this->enchantments[$enchId] = $level;
}
public static function sortEnchantments(&$enchantments){
  ksort($enchantments, SORT_NUMERIC);
}


// parse enchantments string from db
public static function parseEnchantments($enchStr) {
  // already an array
  if(is_array($enchStr)) return($enchStr);
  if(gettype($enchStr) != 'string') return(array());
  if(empty($enchStr)) return(array());
  $output = array();
  $lines = explode(',', $enchStr);
  foreach($lines as $line){
    $parts = explode(':', $line);
    if(count($parts) != 2) continue;
//    $output[(int)$parts[0]] = (int)$parts[1];
    $output[$parts[0]] = $parts[1];
  }
  return($output);
}
// compress enchantments for db storage
public static function compressEnchantments($enchantments){
  if(!is_array($enchantments)) return('');
  if(count($enchantments) == 0) return('');
  $output = '';
  foreach($enchantments as $enchId => $level){
  	if(!empty($output)) $output .= ',';
    $output .= ($enchId).':'.($level);
  }
  return($output);
}


// display item html
public function getDisplay(){
  return(ItemFuncs::getDisplay(
    $this->tableRowId,
    $this->itemId,
    $this->itemDamage,
    $this->qty,
    $this->enchantments
  ));
}


// get item name
public function getItemName(){
  if($this->itemId <= 0) return('');
  return(ItemFuncs::getItemName($this->itemId, $this->itemDamage));
}
// get item title
public function getItemTitle(){
  if($this->itemId <= 0) return('');
  return(ItemFuncs::getItemTitle($this->itemId, $this->itemDamage));
}


// get item icon file
public function getItemImageUrl(){
  if($this->itemId <= 0) return('');
  return(ItemFuncs::getItemImageUrl($this->itemId, $this->itemDamage));
}


// get damage/charged percent string
public function getDamagedChargedStr(){
  return(ItemFuncs::getDamagedChargedStr($this->itemId, $this->itemDamage));
}

// item market price of one item
public function getMarketPrice(){
  return($this->marketPrice);
}

// item market price of the whole item stack
public function getMarketPriceTotal(){
  return($this->marketPriceTotal);
}
// get percent damaged
//public function getPercentDamaged(){
//  if($this->itemId <= 0) return('');
//  $item = ItemFuncs::getItemArray($this->itemId);
//  if(!isset($item['damage'])) return('');
//  return(ItemFuncs::getPercentDamagedStr($this->itemDamage,$item['damage']));
//}
//public function getPercentDamageStr(){
//  $damaged = $this->getPercentDamaged();
//  if( ((string)$damaged) == '0') return('Brand New!');
//  else                           return((string)$damaged);
//}
// get percent charged
//public function getItemDamageStr(){
//  return(ItemFuncs::getPercentChargedStr($this->itemId, $this->itemDamage));
////  $damaged = $this->getPercentDamaged();
//  if( ((string)$damaged) == '0') return('Fully Charged!');
//  else                           return((string)$damaged);
//}


}
?>
