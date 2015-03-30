<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
// admin page
$outputs=array();


$outputs['body top']='
';


$outputs['body']='
<p>ADMIN PAGE</p>
<p>This is a temporary page</p>
';


$outputs['body bottom']='
';


return($outputs);
?>