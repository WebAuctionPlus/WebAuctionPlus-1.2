<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
// sales log class
class LogSales{

const LOG_NEW    = 'new';
const LOG_SALE   = 'sale';
const LOG_CANCEL = 'cancel';

const SALE_BUYNOW  = 'buynow';
const SALE_AUCTION = 'auction';
const SALE_SERVER  = 'server';


public static function addLog($logType, $saleType, $sellerId, $buyerId, $Item, $price, $allowBids, $currentWinner, $alert=0){global $config;
  $query = "INSERT INTO `".$config['table prefix']."LogSales` ( ".
           "`logType`, `saleType`, `timestamp`, `itemType`, `itemId`, `itemDamage`, `itemTitle`, `enchantments`, `sellerId`, `buyerId`, `qty`, `price`, `alert` ) VALUES ( ".
           (($logType  == self::LOG_NEW     || $logType  == self::LOG_SALE     || $logType == self::LOG_CANCEL)   ? "'".mysql_san($logType )."'" : 'NULL' ).", ".
           (($saleType == self::SALE_BUYNOW || $saleType == self::SALE_AUCTION || $saleType == self::SALE_SERVER) ? "'".mysql_san($saleType)."'" : 'NULL' ).", ".
           "NOW(), ".
           ( $Item     == NULL ? 'NULL' : "'".mysql_san($Item->getItemType())."'"               ).", ".
           ( $Item     == NULL ? '0'    : ((int) $Item->getItemId())                            ).", ".
           ( $Item     == NULL ? '0'    : ((int) $Item->getItemDamage())                        ).", ".
           ( $Item     == NULL ? 'NULL' : "'".mysql_san($Item->getItemTitle())."'"              ).", ".
           ( $Item     == NULL ? 'NULL' : "'".mysql_san($Item->getEnchantmentsCompressed())."'" ).", ".
           ( $sellerId == NULL ? 'NULL' : "'".mysql_san($sellerId)."'"                          ).", ".
           ( $buyerId  == NULL ? 'NULL' : "'".mysql_san($buyerId)."'"                           ).", ".
           ( $Item     == NULL ? '0'    : ((int) $Item->getItemQty())                           ).", ".
           ((double) $price).", ".
           ((int) $alert)." )";
  $result = RunQuery($query, __file__, __line__);
  if(!$result || mysql_affected_rows()==0){echo '<p style="color: red;">Error logging sale!</p>'; exit();}
}


}
?>