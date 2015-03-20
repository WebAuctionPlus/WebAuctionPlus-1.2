<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
// sell page
$outputs=array();


$outputs['header']='
<script type="text/javascript" language="javascript">
function updateTotal(thisfield,otherfieldid){
  otherfield = document.getElementById(otherfieldid);
  document.getElementById("pricetotal").innerHTML = (thisfield.value * otherfield.value).toFixed(2);
}
</script>
';


$outputs['body']='
<br /><br />
<form action="./" method="post">
{token form}
<input type="hidden" name="page"     value="{page}" />
<input type="hidden" name="action"   value="newauction" />
<input type="hidden" name="lastpage" value="{lastpage}" />
<input type="hidden" name="id"       value="{item id}" />
<table border="0" cellpadding="0" cellspacing="0" id="createauctionTable">
<tr><td align="center">{messages}</td></tr>

<tr><td align="center"><h2>Create a New Auction</h2></td></tr>
<tr><td align="center"><div class="input" style="width: 150px; padding-top: 15px; padding-bottom: 15px; text-align: center;">{item display}</div></td></tr>
<tr><td height="20"></td></tr>

<tr><td align="center"><b>You have <font size="+2">{have qty}</font> items</b></td></tr>
<tr><td><table border="0" cellpadding="0" cellspacing="10" align="center">

<tr>
  <td align="right"><b>Quantity:</b></td>
  <td><div style="position: absolute; margin-top: 10px; margin-left: 8px; font-size: larger; font-weight: bold;">x</div>'.
    '<input type="text" name="qty" value="{qty}" id="qty" class="input" style="width: 160px; text-align: center;" '.
    'onkeypress="return numbersonly(this, event);" onkeyup="updateTotal(this,\'price\');" /></td>
</tr>
<tr>
  <td align="right"><b>Price Each:</b></td>
  <td><div style="position: absolute; margin-top: 8px; margin-left: 8px; font-size: larger; font-weight: bold;">{currency prefix}</div>'.
    '<input type="text" name="price" value="{price each}" id="price" class="input" style="width: 160px; text-align: center;" '.
    'onkeypress="return numbersonly(this, event);" onkeyup="updateTotal(this,\'qty\');" />'.
    '<b>&nbsp;{currency postfix}</b></td>
</tr>
<tr>
  <td align="right"><b>Price Total:</b></td>
  <td><div style="position: absolute; margin-top: 8px; margin-left: 8px; font-size: larger; font-weight: bold;">{currency prefix}</div>'.
    '<div id="pricetotal" class="input" style="float: left; width: 160px; text-align: center; font-size: larger; font-weight: bold;">&nbsp;</div>'.
    '<div style="margin-top: 8px;"><b>&nbsp;{currency postfix}</b></div></td>
</tr>
</table></td></tr>

<tr><td height="20"></td></tr>

<tr><td colspan="2" align="center">
  <input type="submit" value="Sell Fixed Price" class="input" />
<!--
  &nbsp;
  <input type="submit" value="Create Auction" class="input" />
-->
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
<h2 style="color: #ff0000; text-align: center;">{message}</h2>
';


return($outputs);
?>
