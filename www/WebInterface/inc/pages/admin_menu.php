<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
// admin menu


// check admin permission
global $user;
if(!$user->hasPerms('isAdmin')) {
  ForwardTo('./', 0);
  exit();
}


$outputs = RenderHTML::LoadHTML('pages/admin_menu.php');
return(
  $outputs['menu']
);


?>