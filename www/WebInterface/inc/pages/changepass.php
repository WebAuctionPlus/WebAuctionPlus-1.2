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
    $_SESSION['error'][] = 'Passwords don\'t match. Please try again.';
    return FALSE;
  }
  // check password length
  if(strlen($password) < 6) {
    $_SESSION['error'][] = 'Password is to short, must be at least 6 characters long.';
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
  if(!is_array($outputs)) {echo 'Failed to load html!'; exit();}  // display error
  $messages = '';
  if(isset($_SESSION['error'])) {
    if(is_array($_SESSION['error'])) {
      foreach($_SESSION['error'] as $msg)
        $messages .= str_replace('{message}', $msg, $outputs['error']);
    } else{
      $messages .= str_replace('{message}', $_SESSION['error'], $outputs['error']);
    }
    unset($_SESSION['error']);
  }
  $outputs['body'] = str_replace('{messages}', $messages, $outputs['body']);
  unset($messages);
  return($outputs['body']);
}


?>