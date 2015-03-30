<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
// admin page


// check admin permission
if(!$user->hasPerms('isAdmin')) {
  ForwardTo('./', 0);
  exit();
}


// need to change temp pass
if($config['user']->isTempPass()) {
  ForwardTo('./?page=changepass', 0);
  exit();
}


function RenderPage_admin(){global $config;
  $config['title'] = 'Admin';
  $menu = include('admin_menu.php');
  $outputs = RenderHTML::LoadHTML('pages/admin.php');
  if(!is_array($outputs)) {echo 'Failed to load html!'; exit();}
  return(
    $menu.
    $outputs['body top'].
    $outputs['body'].
    $outputs['body bottom']
  );
}


?>