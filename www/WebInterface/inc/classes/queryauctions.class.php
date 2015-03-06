<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
class QueryAuctions{

protected $result = FALSE;
protected $result_price = FALSE;


// get auctions
public static function QueryCurrent(){
  $class = new QueryAuctions();
  $class->doQuery();
  if(!$class->result) return(FALSE);
  return($class);
}
// get my auctions
public static function QueryMy(){global $user;
  if(!$user->isOk()) {$this->result = FALSE; return(FALSE);}
  $class = new QueryAuctions();
  $class->doQuery( "`playerId` = '".mysql_san($user->getId())."'" );
  if(!$class->result) return(FALSE);
  return($class);
}
// query single auction
public static function QuerySingle($id){global $config;
  if($id < 1) {$this->result = FALSE; return(FALSE);}
  $class = new QueryAuctions();
  $class->doQuery( "".$config['table prefix']."Auctions.id = ".((int)$id) );
  if(!$class->result) return(FALSE);
  return($class->getNext());
}
// query
protected function doQuery($WHERE=''){global $config;
  $query = "SELECT ".(getVar('ajax','bool')?"SQL_CALC_FOUND_ROWS ":'').
           "".$config['table prefix']."Auctions.id, `playerId`, `playerName`, `uuid`, `itemId`, `itemDamage`, `itemData`, `qty`, `enchantments`, ".
           "`price`, UNIX_TIMESTAMP(`created`) AS `created`, `allowBids`, `currentBid`, `currentWinner` ".
           "FROM `".$config['table prefix']."Auctions` JOIN `".$config['table prefix']."Players` ON ".$config['table prefix']."Auctions.playerId = ".$config['table prefix']."Players.id ";
  // where
  if(is_array($WHERE)){
    $query_where = $WHERE;
  }else{
    $query_where = array();
    if(!empty($WHERE)) $query_where[] = $WHERE;
  }
  // ajax search
  $sSearch = getVar('sSearch');
  if(!empty($sSearch))
    $query_where[] = "(`itemTitle` LIKE '%".mysql_san($sSearch)."%' OR ".
                     "`playerName` LIKE '%".mysql_san($sSearch)."%')";
  // build where string
  if(count($query_where) == 0) $query_where = '';
  else $query_where = 'WHERE '.implode(' AND ', $query_where);
  // ajax sorting
  $query_order = '';
  if(isset($_GET['iSortCol_0'])){
  	$order_cols = array(
  	  0 => "`itemTitle`",
  	  1 => "`playerName`",
  	  2 => "`price`",
  	  3 => "(`price` * `qty`)",
  	  4 => "1", // market
  	  5 => "`qty`",
  	);
    $iSortingCols = getVar('iSortingCols', 'int');
    for($i = 0; $i < $iSortingCols; $i++){
      $iSortCol = getVar('iSortCol_'.$i, 'int');
      if(!getVar('bSortable_'.$iSortCol, 'bool')) continue;
      if(!isset($order_cols[$iSortCol])) continue;
      if(!empty($query_order)) $query_order .= ', ';
      $query_order .= $order_cols[$iSortCol].' '.mysql_san(getVar('sSortDir_'.$i, 'str'));
    }
  }
  if(empty($query_order)) $query_order = "".$config['table prefix']."Auctions.id ASC";
  $query_order = ' ORDER BY '.$query_order;
  // pagination
  $query_limit = '';
  if(isset($_GET['iDisplayStart'])){
    $start =  getVar('iDisplayStart',  'int');
    $length = getVar('iDisplayLength', 'int');
    if($length != -1) $query_limit = ' LIMIT '.((int)$start).', '.((int)$length);
  }
  $query .= $query_where.
            $query_order.
            $query_limit;
  $this->result = RunQuery($query, __file__, __line__);  
}
public static function TotalDisplaying(){
  $query = "SELECT FOUND_ROWS()";
  $result = RunQuery($query, __file__, __line__);
  if(!$result) return(0);
  $row = mysql_fetch_row($result);
  if(count($row) != 1) return(0);
  return($row[0]);
}
public static function TotalAllRows(){global $config;
  $query = "SELECT COUNT(*) as `count` FROM `".$config['table prefix']."Auctions`";
  $result = RunQuery($query, __file__, __line__);
  if(!$result) return(0);
  $row = mysql_fetch_row($result);
  if(count($row) != 1) return(0);
  return($row[0]);
}


// get next auction
public function getNext(){
  global $config;
  if(!$this->result) return(FALSE);
  $row = mysql_fetch_assoc($this->result);
  if(!$row) return(FALSE);
  if($this->result){ 
    $query_price = "SELECT AVG(price) AS MarketPrice FROM `".$config['table prefix']."LogSales` WHERE ".
               "`itemId` = ".    ((int) $row['itemId'])." AND ".
               "`itemDamage` = ".((int) $row['itemDamage'])." AND ".
               "`enchantments` = '".mysql_san($row['enchantments'])."' AND ".
               "`logType` =      'sale'".
               "ORDER BY `id` DESC LIMIT 10";
    $this->result_price = RunQuery($query_price, __file__, __line__);
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
  // new auction dao
  return(new AuctionDAO(
    $row['id'],
    $row['playerName'],
    $row['uuid'],
    $row['playerId'],
    new ItemDAO(
      -1,
      $row['itemId'],
      $row['itemDamage'],
      $row['itemData'],
      $row['qty'],
      $marketPrice,
      $marketPrice_total,
      $row['enchantments']
    ),
    $row['price'],
    $row['created'],
    $row['allowBids']!=0,
    $row['currentBid'],
    $row['currentWinner']
  ));
}


}
?>
