<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
// server shops page


global $config;
// render ajax
if(getVar('ajax', 'boolean')){
	RenderPage_servershops_ajax();
	exit();
}


// need to change temp pass
if($config['user']->isTempPass()) {
  ForwardTo('./?page=changepass', 0);
  exit();
}


// buy from server shop
if(strtolower($config['action']) == 'buy'){
  CSRF::ValidateToken();
  // inventory is locked
  if($config['user']->isLocked()){
    echo '<center><h2>Your inventory is currently locked.<br />Please close your in game inventory and try again.</h2><br /><a href="'.getLastPage().'">Back to last page</a></center>';
    ForwardTo(getLastPage(), 4);
    exit();
  }
  // buy from server shop
  if(AuctionFuncs::BuyServerShop(
    getVar('shopid', 'int', 'post'),
    getVar('qty',    'int', 'post')
  )){
    echo '<center><h2>Item purchased successfully!</h2><br /><a href="'.getLastPage().'">Back to last page</a></center>';
    ForwardTo(getLastPage(), 2);
    exit();
  }
  echo $config['error']; exit();
}
// sell to server shop
if(strtolower($config['action']) == 'sell'){
  CSRF::ValidateToken();
  // inventory is locked
  if($config['user']->isLocked()){
    echo '<center><h2>Your inventory is currently locked.<br />Please close your in game inventory and try again.</h2><br /><a href="'.getLastPage().'">Back to last page</a></center>';
    ForwardTo(getLastPage(), 4);
    exit();
  }
  // sell to server shop
  if(AuctionFuncs::SellServerShop(
    getVar('shopid', 'int', 'post'),
    getVar('qty',    'int', 'post')
  )){
    echo '<center><h2>Item sold successfully!</h2><br /><a href="'.getLastPage().'">Back to last page</a></center>';
    ForwardTo(getLastPage(), 2);
    exit();
  }
  echo $config['error']; exit();
}
if($config['action']=='cancel'){
  CSRF::ValidateToken();
  // inventory is locked
  if($config['user']->isLocked()){
    echo '<center><h2>Your inventory is currently locked.<br />Please close your in game inventory and try again.</h2><br /><a href="'.getLastPage().'">Back to last page</a></center>';
    ForwardTo(getLastPage(), 4);
    exit();
  }
  // cancel server shop
  if(AuctionFuncs::CancelServerShop(
    getVar('shopid', 'int', 'post')
  )){
    echo '<center><h2>Server Shop canceled!</h2><br /><a href="'.getLastPage().'">Back to last page</a></center>';
    ForwardTo(getLastPage(), 2);
    exit();
  }
  echo $config['error']; exit();
}


// render page (ajax/json)
function RenderPage_servershops_ajax(){global $config,$html;
  //file_put_contents('ajax_get.txt',print_r($_GET,TRUE));
  header('Content-Type: text/plain');
  // list server shops
  $shops = QueryAuctions::QueryServerShops();
  $TotalDisplaying = QueryAuctions::TotalDisplaying();
  $TotalAllRows    = QueryAuctions::TotalAllRows();
  $outputRows = "{\n".
    "\t".'"iTotalDisplayRecords" : '.$TotalDisplaying.",\n".
    "\t".'"iTotalRecords" : '.       $TotalAllRows   .",\n".
    "\t".'"sEcho" : '.((int)getVar('sEcho','int'))   .",\n".
    "\t".'"aaData" : ['                              ."\n";
  if($TotalDisplaying < 1){
    unset($shops);
  } else {
    $outputRows .= "\t{\n";
    $count = 0;
    while(TRUE){
      $shop = $shops->getNext();
      if(!$shop) break;
      $Item = $shop->getItem();
      if(!$Item) continue;
      if($count != 0) $outputRows .= "\t},\n\t{\n";
      $count++;
      $data = array(
        'item'        => $Item->getDisplay(),
        'buy price'   => FormatPrice($shop->getPriceBuy()),
        'sell price'  => FormatPrice($shop->getPriceSell()),
        'buy/sell'    => '',
      );
      // buy/sell button
      if($config['user']->hasPerms('canBuy') || $config['user']->hasPerms('canSell')) {
        $data['buy/sell'] = '
<form action="./" method="post">
'.CSRF::getTokenForm().'
<input type="hidden" name="page"      value="'.$config['page'].'" />
<input type="hidden" name="shopid" value="'.((int)$shop->getTableRowId()).'" />
<input type="text" name="qty" value="64" onkeypress="return numbersonly(this, event);" '.
'class="input" style="width: 60px; margin-bottom: 5px; text-align: center;" /><br />
'.($config['user']->hasPerms('canBuy') ? '
<input type="submit" name="action" value="Buy"  class="button" />
':'').
($config['user']->hasPerms('canSell') ? '
<input type="submit" name="action" value="Sell" class="button" />
':'').'
</form>
';
      }
      // cancel button
      if($config['user']->hasPerms('isAdmin')) {
        $data['isAdmin'] = '
<form action="./" method="post">
'.CSRF::getTokenForm().'
<input type="hidden" name="page"      value="'.$config['page'].'" />
<input type="hidden" name="action"    value="cancel" />
<input type="hidden" name="shopid" value="'.((int)$shop->getTableRowId()).'" />
<input type="submit" value="Cancel" class="button" />
</form>
';
      }
      // sanitize
      $data = str_replace(
        array('/' , '"' , "\r", "\n"),
        array('\/', '\"', ''  , '\n'),
        $data
      );
      $rowClass = 'gradeU';
      $outputRows .= "\t\t".'"DT_RowClass":"'.$rowClass.'",'."\n";
      $i = -1;
      foreach($data as $v) {
        $i++;
        if($i != 0) $outputRows .= ",\n";
        $outputRows .= "\t\t".'"'.$i.'":"'.$v.'"';
      }
      $outputRows .= "\n";
    }
    unset($shops, $Item);
    $outputRows .= "\t}\n";
  }
  $outputRows .= ']}'."\n";
  //file_put_contents('ajax_output.txt',$outputRows);
  echo $outputRows;
  exit();
}


function RenderPage_servershops(){global $config,$html;
  $config['title'] = 'Server Shops';
  // load page html
  $outputs = RenderHTML::LoadHTML('pages/servershops.php');
  if(!is_array($outputs)) {echo 'Failed to load html!'; exit();}
  // load javascript
  $html->addToHeader($outputs['header']);
  // display error
  $messages = '';
  if(isset($config['error']))
    $messages .= str_replace('{message}', $config['error'], $outputs['error']);
  if(isset($_SESSION['error'])) {
    $messages .= str_replace('{message}', $_SESSION['error'], $outputs['error']);
    unset($_SESSION['error']);
  }
  // display success
  if(isset($_SESSION['success'])) {
    $messages .= str_replace('{message}', $_SESSION['success'], $outputs['success']);
    unset($_SESSION['success']);
  }
  $outputs['body top'] = str_replace('{messages}', $messages, $outputs['body top']);
  unset($messages);
  return(
    $outputs['body top']."\n".
    $outputs['body bottom']
  );
}


?>