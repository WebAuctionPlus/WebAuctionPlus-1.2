<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
// my items page


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


function RenderPage_myitems(){global $config,$html;
  $UseAjaxSource = FALSE;
  $config['title'] = 'My Items';
  // load page html
  $outputs = RenderHTML::LoadHTML('pages/myitems.php');
  if(!is_array($outputs)) {echo 'Failed to load html!'; exit();}
  // load javascript
  $html->addToHeader($outputs['header']);
  // display error
  $messages = '';
  if(isset($_SESSION['error'])) {
    if(is_array($_SESSION['error'])) {
      foreach($_SESSION['error'] as $msg)
        $messages .= str_replace('{message}', $msg, $outputs['error']);
    } else {
      $messages .= str_replace('{message}', $_SESSION['error'], $outputs['error']);
    }
    unset($_SESSION['error']);
  }
  // display success
  if(isset($_SESSION['success'])) {
    if(is_array($_SESSION['success'])) {
      foreach($_SESSION['success'] as $msg)
        $messages .= str_replace('{message}', $msg, $outputs['success']);
    } else {
      $messages .= str_replace('{message}', $_SESSION['success'], $outputs['success']);
    }
    unset($_SESSION['success']);
  }
  $outputs['body top'] = str_replace('{messages}', $messages, $outputs['body top']);
  unset($messages);
  // list items
  $Items = QueryItems::QueryInventory($config['user']->getId());
  if($Items == FALSE) {echo 'Unable to query items!'; exit();}
  $outputRows = '';
  while(TRUE) {
  	$Item = $Items->getNext();
  	if(!$Item) break;
    $tags = array(
      'item display'       => $Item->getDisplay(),
      'item row id'        => $Item->getTableRowId(),
      'item qty'           => $Item->getItemQty(),
      'market price each'  => $Item->getMarketPrice(),
      'market price total' => $Item->getMarketPriceTotal(),
//number_format((double)$auction['price'],2)
//number_format((double)($auction['price'] * $Item->qty),2)
//  $marketPrice=getMarketPrice($id, 0);
//  $marketTotal=$marketPrice*$quantity;
//  if($marketPrice==0){
//    $marketPrice='0';
//    $marketTotal='0';
//  }
//  echo '  <tr class="gradeC">'."\n";
      'rowclass'           => 'gradeU',
    );
    $htmlRow = $outputs['body row'];
    RenderHTML::RenderTags($htmlRow, $tags);
    unset($tags);
    $outputRows .= $htmlRow;
  }
  unset($Items, $Item);
  return(
    $outputs['body top']."\n".
    $outputRows."\n".
    $outputs['body bottom']
  );
}


?>