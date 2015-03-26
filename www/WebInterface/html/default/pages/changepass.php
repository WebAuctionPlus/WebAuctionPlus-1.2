<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
// change password page
$outputs=array();


$outputs['body']='
<h1 style="color: #55ff55; text-align: center;"><span style="background-color: black;">&nbsp;Please change your password&nbsp;</span></h1>
{messages}
<div id="login-box">
<div id="login-background"><img src="{path=images}wa_bg_login.png" alt="" /></div>
<form action="./" name="changepassform" method="post">
{token form}
<input type="hidden" name="page"     value="changepass" />
<input type="hidden" name="lastpage" value="{lastpage}" />
<table border="0" cellspacing="0" cellpadding="0" align="center" id="login-table">
  <tr>
    <td align="right"><label    for="'.CHANGEPASS_FORM_PASSWORD.'">Password:&nbsp;</label></td>
    <td><input type="password" name="'.CHANGEPASS_FORM_PASSWORD.'" value="" class="input" size="30" tabindex="1" id="'.CHANGEPASS_FORM_PASSWORD.'" /></td>
  </tr>
  <tr><td style="height: 10px;"></td></tr>
  <tr>
    <td align="right"><label    for="'.CHANGEPASS_FORM_CONFIRM.'">Confirm:&nbsp;</label></td>
    <td><input type="password" name="'.CHANGEPASS_FORM_CONFIRM.'" value="" class="input" size="30" tabindex="2" id="'.CHANGEPASS_FORM_CONFIRM.'" /></td>
  </tr>
  <tr><td style="height: 0px;"></td></tr>
  <tr><td colspan="2" align="center"><input type="submit" name="Submit" value="Submit" class="button" tabindex="3" /></td>
  </tr>
</table>
</form>
<script type="text/javascript">
function formfocus() {
  document.getElementById(\''.CHANGEPASS_FORM_PASSWORD.'\').focus();
}
window.onload = formfocus;
</script>
</div>
';


$outputs['error']='
<h2 style="color: #ff0000; text-align: center;"><span style="background-color: black;">&nbsp;{message}&nbsp;</span></h2>
';


return($outputs);
?>