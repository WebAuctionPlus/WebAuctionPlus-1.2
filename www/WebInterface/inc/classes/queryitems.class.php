<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
class QueryItems{

protected $result = FALSE;
protected $result_price = FALSE;


// query inventory item stacks
public static function QueryInventory($playerId){
  if(empty($playerId)) {$this->result = FALSE; return(FALSE);}
  $class = new QueryItems();
  $class->doQuery("`playerId` = '".mysql_san($playerId)."'");
  if(!$class->result) return(FALSE);
  return($class);
}
// query single item stack
public static function QuerySingle($playerId, $id){
  if(empty($playerId)) {$this->result = FALSE; return(FALSE);}
  $class = new QueryItems();
  $class->doQuery("`playerId` = '".mysql_san($playerId)."' AND `id` = ".((int)$id));
  if(!$class->result) return(FALSE);
  return($class->getNext());
}
// query
protected function doQuery($WHERE){global $config;
  if(empty($WHERE)) {$this->result = FALSE; return;}
  $query="SELECT `id`, `itemId`, `itemDamage`, `itemData`, `qty`, `enchantments` ".
         "FROM `".$config['table prefix']."Items` ".
         "WHERE ".$WHERE." ORDER BY `id` ASC";
  $this->result = RunQuery($query, __file__, __line__);  
}


// get next item
public function getNext(){global $config;
  if(!$this->result) return(FALSE);
  $row = mysql_fetch_assoc($this->result);
  if(!$row) return(FALSE);
  if($this->result){ 
    $query = "SELECT AVG(price) AS MarketPrice FROM `".$config['table prefix']."LogSales` WHERE ".
               "`itemId` = ".    ((int) $row['itemId'])." AND ".
               "`itemDamage` = ".((int) $row['itemDamage'])." AND ".
               "`enchantments` = '".mysql_san($row['enchantments'])."' AND ".
               "`logType` =      'sale'".
               "ORDER BY `id` DESC LIMIT 10";
    $this->result_price = RunQuery($query, __file__, __line__);
  }
  if($this->result_price){
      $row_price = mysql_fetch_assoc($this->result_price);
      if($row_price){
          $marketPrice = $row_price['MarketPrice'];
          $marketPrice_total = $marketPrice * $row['qty'];
      } else {
          $marketPrice = "--";
          $marketPrice_total = "--";
      }
  }
  // new item dao
  return(new ItemDAO(
    $row['id'],
    $row['itemId'],
    $row['itemDamage'],
    $row['itemData'],
    $row['qty'],
    FormatPrice($marketPrice),
    FormatPrice($marketPrice_total),
    $row['enchantments']
  ));
}


}
?>
