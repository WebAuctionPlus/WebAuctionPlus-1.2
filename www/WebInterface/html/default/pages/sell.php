<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
// sell page
$outputs=array();


global $config;
if($config['action'] == 'fixedprice')
  $StartTabIndex = 0;
else
if($config['action'] == 'auction')
  $StartTabIndex = 1;
else
if($config['action'] == 'servershop')
  $StartTabIndex = 2;
else
  $StartTabIndex = 0;


$outputs['header']='
<script type="text/javascript" language="javascript">
function updateTotal(thisfield,otherfieldid){
  otherfield = document.getElementById(otherfieldid);
  document.getElementById("pricetotal").innerHTML = (thisfield.value * otherfield.value).toFixed(2);
}
</script>
<script type="text/javascript" language="javascript">
// init
function SellTabsInit() {
  var tabs = document.getElementsByClassName(\'SellTypeTab\');
  SellTypeTabs = [];
  for(var i = 0; i< tabs.length; i++) {
    SellTypeTabs[i] = tabs[i];
  }
  // select default
  SelectSellType(
    SellTypeTabs['.$StartTabIndex.']
  );
}
// select
function SelectSellType(selected) {
//  if(selected.tagName != \'td\')
//    selected = selected.parentElement;
  // select/deselect tabs
  for(var i = 0; i < SellTypeTabs.length; i++) {
    var name = SellTypeTabs[i].getAttribute(\'id\');
    var isselected = (striptags(selected.innerHTML) == striptags(SellTypeTabs[i].innerHTML));
    if(isselected) {
      // selected
      SellTypeTabs[i].className = \'SellTypeTabSelected\';
    } else {
      // not selected
      SellTypeTabs[i].className = \'SellTypeTab\';
    }
    // show/hide elements
    document.getElementById(name+\'Title\').style.display  = (isselected ? \'block\' : \'none\');
    document.getElementById(name+\'Submit\').style.display = (isselected ? \'block\' : \'none\');
    if(name == \'TypeFixed\') {
      if(isselected)
        document.getElementById(\'selltype\').setAttribute(\'value\', \'fixedprice\');
      document.getElementById(\'FixedTypePriceEach\').style.display  = (isselected ? \'\' : \'none\');
      document.getElementById(\'FixedTypePriceTotal\').style.display = (isselected ? \'\' : \'none\');
    }
    if(name == \'TypeAuction\') {
      if(isselected)
        document.getElementById(\'selltype\').setAttribute(\'value\', \'auction\');
      document.getElementById(\'AuctionTypePriceStart\').style.display = (isselected ? \'\' : \'none\');
    }
{if permission[isAdmin]}
    if(name == \'TypeServer\') {
      if(isselected)
        document.getElementById(\'selltype\').setAttribute(\'value\', \'servershop\');
      document.getElementById(\'QtyAvailable\').style.display = (isselected ? \'none\' : \'\');
      document.getElementById(\'ServerShopQtyNote\').style.display   = (isselected ? \'\' : \'none\');
      document.getElementById(\'ServerTypePriceBuy\').style.display  = (isselected ? \'\' : \'none\');
      document.getElementById(\'ServerTypePriceSell\').style.display = (isselected ? \'\' : \'none\');
    }
{endif}
  }
}

// strip tags from text
function striptags(text) {
  return text.replace(/(<([^>]+)>)/ig, \'\');
}

// on page load
if(window.attachEvent) {
  window.attachEvent(\'onload\', SellTabsInit);
} else {
  if(window.onload) {
    var curronload = window.onload;
    var newonload = function() {
      curronload();
      SellTabsInit();
    };
    window.onload = newonload;
  } else {
    window.onload = SellTabsInit;
  }
}
</script>
';


$outputs['body']='
<br /><br />
<form action="./" method="post">
{token form}
<input type="hidden" name="page"     value="{page}" />
<input type="hidden" name="lastpage" value="{lastpage}" />
<input type="hidden" name="action" value="" id="selltype" />
<input type="hidden" name="id"       value="{item id}" />
<table border="0" cellpadding="0" cellspacing="0" id="CreateSellTable">

<!-- sell type tabs -->
<tr><td><table border="0" cellpadding="0" cellspacing="0" id="CreateSellTypeTable">
<tr>
  <td class="SellTypeTab" id="TypeFixed"><a href="#" onClick="javascript: SelectSellType(this); return false;">Fixed Price</a></td>
  <td class="SellTypeTab" id="TypeAuction"><a href="#" onClick="javascript: SelectSellType(this); return false;">Auction</a></td>
{if permission[isAdmin]}
  <td class="SellTypeTab" id="TypeServer"><a href="#" onClick="javascript: SelectSellType(this); return false;">Server Shop</a></td>
{endif}
</tr>
</table></td></tr>

<tr><td align="center">{messages}</td></tr>

<tr><td align="center" style="display: none;" id="TypeFixedTitle"><h2>Sell Fixed Price</h2></td></tr>
<tr><td align="center" style="display: none;" id="TypeAuctionTitle"><h2>Create a New Auction</h2></td></tr>
{if permission[isAdmin]}
<tr><td align="center" style="display: none;" id="TypeServerTitle"><h2>Create Server Shop</h2></td></tr>
{endif}

<tr><td align="center"><div class="input" style="width: 150px; padding-top: 15px; padding-bottom: 15px; text-align: center;">{item display}</div></td></tr>
<tr><td height="20"></td></tr>

<tr style="display: none;" id="QtyAvailable"><td align="center"><b>You have <font size="+2">{have qty}</font> items</b></td></tr>
{if permission[isAdmin]}
<tr style="display: none;" id="ServerShopQtyNote"><td align="center"><font size="-1"><b>(Set quantity to 0 for unlimited)</b></font></td></tr>
{endif}
<tr><td><table border="0" cellpadding="0" cellspacing="10" align="center">

<tr>
  <td align="right"><b>Quantity:</b></td>
  <td><div style="position: absolute; margin-top: 10px; margin-left: 8px; font-size: larger; font-weight: bold;">x</div>'.
    '<input type="text" name="qty" value="{qty}" id="qty" class="input" style="width: 160px; text-align: center;" '.
    'onkeypress="return numbersonly(this, event);" onkeyup="updateTotal(this,\'priceFixed\');" /></td>
</tr>

<!-- fixed price -->
<tr style="display: none;" id="FixedTypePriceEach">
  <td align="right"><b>Price Each:</b></td>
  <td><div style="position: absolute; margin-top: 8px; margin-left: 8px; font-size: larger; font-weight: bold;">{currency prefix}</div>'.
    '<input type="text" name="priceFixed" value="{price fixed}" id="priceFixed" class="input" style="width: 160px; text-align: center;" '.
    'onkeypress="return numbersonly(this, event);" onkeyup="updateTotal(this,\'qty\');" /><b>&nbsp;{currency postfix}</b></td>
</tr>
<tr style="display: none;" id="FixedTypePriceTotal">
  <td align="right"><b>Price Total:</b></td>
  <td><div style="position: absolute; margin-top: 8px; margin-left: 8px; font-size: larger; font-weight: bold;">{currency prefix}</div>'.
    '<div id="pricetotal" class="input" style="float: left; width: 160px; text-align: center; font-size: larger; font-weight: bold;">&nbsp;</div>'.
    '<div style="margin-top: 8px;"><b>&nbsp;{currency postfix}</b></div></td>
</tr>

<!-- auction price -->
<tr style="display: none;" id="AuctionTypePriceStart">
  <td align="right"><b>Start Price:</b></td>
  <td><div style="position: absolute; margin-top: 8px; margin-left: 8px; font-size: larger; font-weight: bold;">{currency prefix}</div>'.
    '<input type="text" name="priceStart" value="{price start}" id="priceStart" class="input" style="width: 160px; text-align: center;" '.
    'onkeypress="return numbersonly(this, event);" />'.
    '<b>&nbsp;{currency postfix}</b></td>
</tr>

{if permission[isAdmin]}
<!-- server shop price -->
<tr style="display: none;" id="ServerTypePriceBuy">
  <td align="right"><b>Buy Price:</b></td>
  <td><div style="position: absolute; margin-top: 8px; margin-left: 8px; font-size: larger; font-weight: bold;">{currency prefix}</div>'.
    '<input type="text" name="priceBuy" value="{price buy}" id="priceStart" class="input" style="width: 160px; text-align: center;" '.
    'onkeypress="return numbersonly(this, event);" />'.
    '<b>&nbsp;{currency postfix}</b></td>
</tr>
<tr style="display: none;" id="ServerTypePriceSell">
  <td align="right"><b>Sell Price:</b></td>
  <td><div style="position: absolute; margin-top: 8px; margin-left: 8px; font-size: larger; font-weight: bold;">{currency prefix}</div>'.
    '<input type="text" name="priceSell" value="{price sell}" id="priceStart" class="input" style="width: 160px; text-align: center;" '.
    'onkeypress="return numbersonly(this, event);" />'.
    '<b>&nbsp;{currency postfix}</b></td>
</tr>
{endif}

</table></td></tr>
<tr><td height="20"></td></tr>

<tr><td colspan="2" align="center">
  <input type="submit" value="Sell Fixed Price"   class="input" id="TypeFixedSubmit"   style="display: none;" />
  <input type="submit" value="Start Auction"      class="input" id="TypeAuctionSubmit" style="display: none;" />
{if permission[isAdmin]}
  <input type="submit" value="Create Server Shop" class="input" id="TypeServerSubmit"  style="display: none;" />
{endif}
</td></tr>

<tr><td height="30"></td></tr>
</table>
</form>
<br /><br />
';
// custom descriptions
//if(SettingsClass::getString('Custom Descriptions')) $output.='
//<tr><td colspan="2" align="center">&nbsp;&nbsp;<b>Description:</b> (optional)</td></tr>
//<tr><td height="10"></td></tr>
//<tr><td colspan="2" align="center"><textarea name="desc" class="input" style="width: 80%; height: 55px;" readonly>Coming soon!</textarea></td></tr>
//<tr><td height="30"></td></tr>
//';


$outputs['error']='
<h3 style="color: #ff0000; text-align: center;">{message}</h3>
';


return($outputs);
?>