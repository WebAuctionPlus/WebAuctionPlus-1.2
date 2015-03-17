<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
// login page


//NoPageCache();
// check login
function doCheckLogin(){global $config;
  if(!isset($_POST[LOGIN_FORM_USERNAME]) || !isset($_POST[LOGIN_FORM_PASSWORD])) return NULL;
  $username = trim(stripslashes( @$_POST[LOGIN_FORM_USERNAME] ));
  $password = trim(stripslashes( @$_POST[LOGIN_FORM_PASSWORD] ));
  unset($_POST[LOGIN_FORM_PASSWORD]);
  session_init();
  if(CSRF::isEnabled() && !isset($_SESSION[CSRF::SESSION_KEY])){
    echo '<p style="color: red;">PHP Session seems to have failed!</p>';
    CSRF::ValidateToken();
    exit();
  }
  CSRF::ValidateToken();
  // check hashed password
  $result = $config['user']->doLogin($username, md5($password));
  // try temporary password
  if($result !== TRUE && $_GET['error'] == 'bad login' && strlen($password) < 32) {
    unset($_GET['error']);
    $result = $config['user']->doLogin($username, $password);
    if($result === TRUE && $config['user']->isOk() && getVar('error')=='') {
      $config['user']->isTempPass(TRUE);
    }
  }
  // successful login
  if($result !== FALSE && $config['user']->isOk() && getVar('error')==''){
    $lastpage = getLastPage();
    if(strpos($lastpage,'login') !== FALSE)
      $lastpage = './';
    ForwardTo($lastpage);
    exit();
  }
  unset($username, $password);
  return TRUE;
}
doCheckLogin();


function RenderPage_login(){global $config,$html;
  $config['title'] = 'Login';
  $html->setPageFrame('basic');
  // load page html
  $html->LoadCss('login.css');
  $outputs = RenderHTML::LoadHTML('pages/login.php');
  $html->addTags(array(
    'messages' => '',
    'username' => $config['demo'] ? 'demo' : getVar(LOGIN_FORM_USERNAME),
    'password' => $config['demo'] ? 'demo' : '',
  ));
  // display error
  if(getVar('error') != '') {
    $html->addTags(array(
      'messages' => str_replace('{message}', 'Login Failed', $outputs['error'])
    ));
  }
  return($outputs['body']);
}


?>