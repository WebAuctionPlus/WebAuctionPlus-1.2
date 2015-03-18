<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
// login page


global $config;
// need to log in
if(!$config['user']->isOk()) {
  ForwardTo('./', 0);
  exit();
}


define('CHANGEPASS_FORM_PASSWORD', 'WA_ChangePass_Password');
define('CHANGEPASS_FORM_CONFIRM',  'WA_ChangePass_Confirm');


NoPageCache();
// change password
function doChangePassword(){global $config;
  if(!isset($_POST[CHANGEPASS_FORM_PASSWORD]) || !isset($_POST[CHANGEPASS_FORM_CONFIRM])) return NULL;
  $password = trim(stripslashes( @$_POST[CHANGEPASS_FORM_PASSWORD] ));
  $confirm  = trim(stripslashes( @$_POST[CHANGEPASS_FORM_CONFIRM ] ));
  unset($_POST[CHANGEPASS_FORM_PASSWORD]);
  unset($_POST[CHANGEPASS_FORM_CONFIRM ]);
  session_init();
  if(CSRF::isEnabled() && !isset($_SESSION[CSRF::SESSION_KEY])){
    echo '<p style="color: red;">PHP Session seems to have failed!</p>';
    CSRF::ValidateToken();
    exit();
  }
  CSRF::ValidateToken();
  // check passwords match
  if($password !== $confirm) {
    $_GET['error'] = 'passwords dont match';
    return FALSE;
  }
  // check password length
  if(strlen($password) < 6) {
    $_GET['error'] = 'password to short';
    return FALSE;
  }
  // update password in database
  $result = $config['user']->ChangePassword(md5($password));
  // successful change
  if($result !== FALSE) {
    // password has been changed
    $config['user']->isTempPass(FALSE);
    $lastpage = getLastPage();
    if(strpos($lastpage,'login') !== FALSE || strpos($lastpage,'changepass') !== FALSE)
      $lastpage = './';
    ForwardTo($lastpage);
    exit();
  }
  return FALSE;
}
doChangePassword();


function RenderPage_changepass(){global $config,$html;
  $config['title'] = 'Change Password';
  $html->setPageFrame('basic');
  // load page html
  $html->LoadCss('login.css');
  $outputs = RenderHTML::LoadHTML('pages/changepass.php');
  if(!is_array($outputs)) {echo 'Failed to load html!'; exit();}
  $html->addTags(array(
    'messages' => '',
  ));
  // display error
  if(@$_GET['error'] == 'passwords dont match') {
    $html->addTags(array(
      'messages' => str_replace('{message}', 'Passwords don\'t match. Please try again.', $outputs['error'])
    ));
  }
  if(@$_GET['error'] == 'password to short') {
    $html->addTags(array(
      'messages' => str_replace('{message}', 'Password is to short, must be at least 6 characters long.', $outputs['error'])
    ));
  }
  return($outputs['body']);
}


?>