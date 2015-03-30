<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
// admin menu
$outputs=array();


$outputs['menu']='
<!-- admin menu -->
<div id="admin-menu-box">
  <span>
    <a href="./?page=admin_settings">Settings</a><br />
  </span>
</div>
';


return($outputs);
?>