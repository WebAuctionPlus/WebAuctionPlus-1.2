<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
// my auctions page


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


if($config['action']=='cancel') {
  CSRF::ValidateToken();
  // inventory is locked
  if($config['user']->isLocked()) {
    $_SESSION['error'][] = 'Your inventory is currently locked.<br />Please close your in game inventory and try again.';
  } else {
    // cancel auction
    if(AuctionFuncs::CancelAuction(
      getVar('auctionid','int','post')
    )){
      $_SESSION['success'][] = 'Auction canceled!';
      ForwardTo(getLastPage(), 0);
      exit();
    }
  }
}


function RenderPage_myauctions(){global $config,$html;
  $UseAjaxSource = FALSE;
  $config['title'] = 'My Auctions';
  // load page html
  $outputs = RenderHTML::LoadHTML('pages/myauctions.php');
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
  // list auctions
  $auctions = QueryAuctions::QueryMy();
  $outputRows = '';
  while(TRUE){
    $auction = $auctions->getNext();
    if(!$auction) break;
    $Item = $auction->getItem();
    if(!$Item) continue;
    $tags = array(
      'auction id'  => (int)$auction->getTableRowId(),
      'seller name' => $auction->getSeller(),
      'item'        => $Item->getDisplay(),
      'qty'         => (int)$Item->getItemQty(),
      'price each'  => FormatPrice($auction->getPrice()),
      'price total' => FormatPrice($auction->getPriceTotal()),
      'created'     => $auction->getCreated(),
//      'expire'      => $auction->getExpire(),
      'market price percent' => FormatPorzent(CalcPorzent($auction->getPrice(), $Item->getMarketPrice())),
      'rowclass'    => 'gradeU',
//TODO:
//allowBids
//currentBid
//currentWinner
    );
//  if($Item->itemType=='tool'){
//    $output.='<br />'.$Item->getDamagedChargedStr();
//    foreach($Item->getEnchantmentsArray() as $ench){
//      $output.='<br /><span style="font-size: smaller;"><i>'.$ench['enchName'].' '.numberToRoman($ench['level']).'</i></span>';
//    }
//  }
//$marketPrice=getMarketPrice($id, 1);
//if($marketPrice>0){
//  $marketPercent=round((($price/$marketPrice)*100), 1);
//}else{
//  $marketPercent='N/A';
//}if($marketPercent=='N/A'){
//  $grade='gradeU';
//}elseif($marketPercent<=50){
//  $grade='gradeA';
//}elseif($marketPercent<=150){
//  $grade='gradeC';
//}else{
//  $grade='gradeX';
//}
    $htmlRow = $outputs['body row'];
    RenderHTML::RenderTags($htmlRow, $tags);
    $outputRows .= $htmlRow;
  }
  unset($auctions, $Item);
  return(
    $outputs['body top']."\n".
    $outputRows."\n".
    $outputs['body bottom']
  );
}


//if($user->hasPerms('canSell')){
//$queryItems=mysql_query("SELECT * FROM WA_Items WHERE player='$user'");
//$output.='
//    <div id="new-auction-box">
//      <h2>Create a new auction</h2>
//      <form action="scripts/newAuction.php" method="post" name="auction">
//      <table style="text-align:left;" width="100%">
//      <tr>
//        <td width="50%"><label>Item</label></td><td width="50%"><select name="Item" class="select">
//';
//while(list($id, $name, $damage, $player, $quantity)=mysql_fetch_row($queryItems)){
//  $marketPrice=getMarketPrice($id, 0);
//  if($marketPrice==0){
//    $marketPrice="N/A";
//  }
//  echo '<option value="'.$id.'">'.getItemName($name, $damage);
//  $queryEnchantLinks=mysql_query("SELECT enchId FROM WA_EnchantLinks WHERE itemId='$id' AND itemTableId=0");
//  while(list($enchId)= mysql_fetch_row($queryEnchantLinks)){
//    $queryEnchants=mysql_query("SELECT * FROM WA_Enchantments WHERE id='$enchId'");
//    while(list($id, $enchName, $enchantId, $level)= mysql_fetch_row($queryEnchants)){
//      echo ' ('.getEnchName($enchantId).' '.numberToRoman($level).")";
//    }
//  }
//  echo '('.$quantity.') (Average '.$currencyPrefix.$marketPrice.$currencyPostfix.')';
//  echo '</option>'."\n";
//}
//
//        </select></td>
//        <tr><td colspan="2" style="text-align:center;">
//        <p>
//          if($isAdmin){ echo "Enter 0 as the quantity for infinite stacks (admins only)"; } 
//        </p>
//        </td></tr>
//        <tr><td><label>Quantity</label></td><td><input name="Quantity" type="text" class="input" size="10" /></td></tr>
//        <tr><td><label>Price (Per Item)</label></td><td><input name="Price" type="text" class="input" size="10" /></td></tr>
//        <!--<tr><td colspan="2" style="text-align:center;"><p>Leave starting bid blank to disable bidding</p></td></tr>
//        <tr><td><label>Starting Bid (Per Item)</label></td><td><input name="MinBid" type="text" class="input" size="10" /></td></tr> -->
//        <tr><td colspan="2" style="text-align:center;"><input name="Submit" type="submit" class="button" /></td></tr>
//        </table>
//      </form>
//    </div>
//';


//oTable.fnGetPosition(
//  $(\'#auctionrow'.((int)$auction['id']).'\').click
//).slideUp();


//DataTables constructor
//oTable = $('#mainTable').dataTable({
//"bProcessing": true,
//"bServerSide": true,
//"iDisplayLength": 50,
//"bLengthChange": false,
//"sAjaxSource": "datatables_comments_list.php",
//"sPaginationType": "full_numbers",
//"aaSorting": [[ 0, "desc" ]],
//"fnDrawCallback": function() {
//  //bind the click handler script to the newly created elements held in the table
//  $('.flagsmileysad').bind('click',auctioncancelclick);
//}
//});


//<script>
//$("button").click(function(){
//  $(this).slideUp();
//});
//</script>


?>