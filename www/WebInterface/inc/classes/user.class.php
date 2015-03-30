<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
// this class handles user accounts and sessions
class UserClass{


protected $UserId      = 0;
protected $UUID        = 0;
protected $Name        = '';
protected $Money       = 0.0;
protected $ItemsSold   = 0;
protected $ItemsBought = 0;
protected $Earnt       = 0.0;
protected $Spent       = 0.0;
protected $permissions = array();
protected $invLocked   = NULL;


function __construct(){global $config;
  session_init();
  $loginUrl = './?page=login';
  if(empty($config['session name'])) $config['session name'] = 'WebAuctionPlus User';
  // check logged in
  if(isset($_SESSION[$config['session name']]))
    $this->doValidate( $_SESSION[$config['session name']] );
  // not logged in (and is required)
  if(SettingsClass::getBoolean('Require Login'))
    if(!$this->isOk() && $config['page'] != 'login'){
      ForwardTo($loginUrl, 0); exit();}
}


// do login
public function doLogin($username, $password){
  if(empty($password))
    return FALSE;
  return($this->doValidate($username, $password));
}
// validate session
private function doValidate($username, $password=FALSE){global $config;
  $this->Name = trim($username);
  if(empty($this->Name)) return(FALSE);
  if($password!==FALSE && empty($password)) return(FALSE);
  // validate player
  $query = "SELECT `id`,`playerName`,`uuid`,`money`,`itemsSold`,`itemsBought`,`earnt`,`spent`,`Permissions`,`Locked` ".
           "FROM `".$config['table prefix']."Players` ".
           "WHERE LOWER(`playerName`)='".mysql_san(strtolower($this->Name))."' ".
           ($password===FALSE?"":"AND `password`='".mysql_san($password)."' ").
           "LIMIT 1";
  $result = RunQuery($query, __file__, __line__);
  if($result){
    if(mysql_num_rows($result)==0){
      $_SESSION[$config['session name']] = '';
      $_SESSION['error']['login failed'] = 'Login Failed';
      return(FALSE);
    }
    $row = mysql_fetch_assoc($result);
    if( strtolower($row['playerName']) != strtolower($this->Name) ) return(FALSE);
    $this->UserId      = ((int)    $row['id']         );
    $this->Name        =           $row['playerName'];
    $this->UUID        =           $row['uuid'];
    $this->Money       = ((double) $row['money']      );
    $this->ItemsSold   = ((int)    $row['itemsSold']  );
    $this->ItemsBought = ((int)    $row['itemsBought']);
    $this->Earnt       = ((double) $row['earnt']      );
    $this->Spent       = ((double) $row['spent']      );
    foreach(explode(',',$row['Permissions']) as $perm)
      $this->permissions[$perm] = TRUE;
    $this->invLocked   = ((boolean)$row['Locked']     );
    $_SESSION[$config['session name']] = $this->Name;
  }else{
    $_SESSION[$config['session name']] = '';
    echo 'Error: '.mysql_error();
    exit();
  }
  // use iconomy table
  if(toBoolean($config['iConomy']['use'])){
    global $db;
    $result = mysql_query("SELECT `balance` FROM `".mysql_san($config['iConomy']['table'])."` WHERE ".
                          "LOWER(`username`)='".mysql_san(strtolower($this->Name))."' LIMIT 1", $db);
    if($result){
      $row = mysql_fetch_assoc($result);
      $this->Money = ((double)$row['balance']);
    }else{
      // table not found
      if(mysql_errno($db) == 1146){
        $config['iConomy']['use'] = FALSE;
      }else echo mysql_error($db);
    }
    unset($result, $row);
  }
    // use Craftconomy table
  if(toBoolean($config['CC']['use'])){
    global $db;
    //$result = mysql_query("SELECT `balance` FROM `".mysql_san($config['CC']['table'])."` WHERE "."LOWER(`username`)='".mysql_san(strtolower($this->Name))."' LIMIT 1", $db);
    $result = mysql_query("SELECT ".mysql_san($config['CC']['prefix'])."_balance.balance AS balance FROM cc3_balance JOIN ".mysql_san($config['CC']['prefix'])."_account ON ".
                          mysql_san($config['CC']['prefix'])."_account.id = ".mysql_san($config['CC']['prefix'])."_balance.username_id ".
                          "WHERE ".mysql_san($config['CC']['prefix'])."_account.uuid = '".mysql_san($this->UUID)."' AND ".
                          "LOWER(".mysql_san($config['CC']['prefix'])."_balance.currency_id) = '".mysql_san($config['CC']['currency'])."' AND LOWER(".mysql_san($config['CC']['prefix'])."_balance.worldName) = '".mysql_san($config['CC']['group'])."';");
    if($result){
      $row = mysql_fetch_assoc($result);
      $this->Money = ((double)$row['balance']);
    }else{
      // table not found
      if(mysql_errno($db) == 1146){
        $config['iConomy']['use'] = FALSE;
      }else echo mysql_error($db);
    }
    unset($result, $row);
  }
  return($this->isOk());
}
public function isOk(){
  return($this->UserId > 0);
}


public function ChangePassword($password) {global $config;
  if(!$this->isOk()) {
    echo '<p>Session not valid, cannot change password!</p>';
    exit();
  }
  if(empty($this->UUID)) {
    echo '<p>UUID not valid, cannot change password!</p>';
    exit();
  }
  $password = trim($password);
  if(strlen($password) != 32) {
    echo '<p>Invalid password argument provided to ChangePassword() function!</p>';
    exit();
  }
  // update password in database
  $query = "UPDATE `".$config['table prefix']."Players` SET ".
           "`password` = '".mysql_san($password)."' ".
           "WHERE `uuid` = '".mysql_san($this->UUID)."' LIMIT 1";
  $result = RunQuery($query, __file__, __line__);
  global $db;
  if(mysql_affected_rows($db) != 1) {
    if(!$this->VerifyPasswordChanged($password)) {
      echo '<p>Failed to update password!</p>';
      exit();
    }
  }
  return TRUE;
}
// if password has already been changed, update returns 0 affected rows
private function VerifyPasswordChanged($password) {global $config;
  if(empty($password)) {
    echo 'Password argument is missing in VerifyPasswordChanged() function!';
    exit();
  }
  $result = mysql_query("SELECT COUNT(*) AS `count` FROM `".$config['table prefix']."Players` ".
                        "WHERE `uuid` = '".mysql_san($this->UUID)."' AND ".
                        "`password` = '".mysql_san($password)."' LIMIT 1");
  if(!$result) return FALSE;
  $row = mysql_fetch_assoc($result);
  return ($row['count'] == 1);
}


public function isTempPass() {
  if(!$this->isOk())
    return FALSE;
  if(!isset($_SESSION['Temp Pass']))
    return FALSE;
  return ($_SESSION['Temp Pass'] != FALSE);
}


// do logout
public function doLogout(){global $config;
  session_init();
  $_SESSION[$config['session name']] = '';
  $_SESSION[CSRF::SESSION_KEY]       = '';
}


// user id
public function getId(){
  return($this->UserId);
}

// user id
public function getUUID(){
  return($this->UUID);
}

// player name
public function getName(){
  return($this->Name);
}
public function nameEquals($name){
  return(strtolower($name) == strtolower($this->getName()));
}


// permissions
public function hasPerms($perms){
  if(empty($perms) || count($this->permissions)==0) return(FALSE);
  if(is_array($perms)){
    if(count($perms) == 0) return(FALSE);
    $hasPerms = TRUE;
    foreach($perms as $perm)
      if(!(boolean)@$this->permissions[$perm])
        $hasPerms = FALSE;
    return($hasPerms);
  }
  return((boolean)@$this->permissions[$perms]);
}


// inventory lock
public function isLocked(){
  if($this->invLocked === null) return(TRUE);
  return($this->invLocked);
}


// money
public function getMoney(){
  return($this->Money);
}
//public function saveMoney($useMySQLiConomy, $iConTableName){
//  if ($useMySQLiConomy){
//    $query = mysql_query("UPDATE `".mysql_san($iConTableName)."` SET ".
//                         "`balance`=".((double)$this->money)" WHERE ".
//                         "`username`='".mysql_san($this->UserName)."' LIMIT 1");
//echo mysql_errno();
//exit();
//  }else{
//    $query = mysql_query("UPDATE `".$config['table prefix']."Players` SET ".
//                         "`money`=".((double)$this->money)." WHERE ".
//                         "`name`='".mysql_san($this->Name)."' LIMIT 1");
//  }
//  if ($useMySQLiConomy){
//    $query = mysql_query("UPDATE $iConTableName SET balance='$this->money' WHERE playername='$this->Name'");
//  }else{
//    $query = mysql_query("UPDATE WA_Players SET money='$this->money' WHERE name='$this->Name'");
//  }
//}
//public function spend($amount, $useMySQLiConomy, $iConTableName){
//  $this->money = $this->money - $amount;
//  $this->saveMoney($useMySQLiConomy, $iConTableName);
//  $this->spent = $this->spent + $amount;
//  $query = mysql_query("UPDATE WA_Players SET spent='$this->spent' WHERE name='$this->name'");
//}
//public function earn($amount, $useMySQLiConomy, $iConTableName){
//  $this->money = $this->money + $amount;
//  $this->saveMoney($useMySQLiConomy, $iConTableName);
//  $this->earnt = $this->earnt + $amount;
//  $query = mysql_query("UPDATE WA_Players SET earnt='$this->earnt' WHERE name='$this->name'");
//}


//TODO: this code doesn't check for failures
public static function MakePayment($fromPlayer, $fromPlayerUUID, $toPlayer, $toPlayerUUID, $amount, $desc='') {
  if(empty($fromPlayer) || empty($toPlayer) || $amount<=0){echo 'Invalid payment amount!'; exit();}
  self::PaymentQuery($toPlayer, $toPlayerUUID,     $amount);
  self::PaymentQuery($fromPlayer, $fromPlayerUUID, 0-$amount);
  // TODO: log transaction
}
public static function PaymentQuery($playerName, $playerUUID, $amount){global $config;
  if(toBoolean($config['iConomy']['use'])) {
    $query = "UPDATE `".mysql_san($config['iConomy']['table'])."` SET ".
             "`balance` = `balance` + ".((double)$amount)." ".
             "WHERE LOWER(`username`)='".mysql_san(strtolower($playerName))."' LIMIT 1;";
  } else
  if(toBoolean($config['CC']['use'])) {
    $query = "UPDATE `".mysql_san($config['CC']['prefix'])."_balance` JOIN ".mysql_san($config['CC']['prefix'])."_account ON ".
             mysql_san($config['CC']['prefix'])."_account.id = ".mysql_san($config['CC']['prefix'])."_balance.username_id ".
             "SET ".mysql_san($config['CC']['prefix'])."_balance.balance = ".mysql_san($config['CC']['prefix'])."_balance.balance + ".((double)$amount)." ".
             "WHERE ".mysql_san($config['CC']['prefix'])."_account.uuid = '".mysql_san($playerUUID)."' AND ".
             "LOWER(".mysql_san($config['CC']['prefix'])."_balance.currency_id) = '".mysql_san(strtolower($config['CC']['currency']))."' ".
             "AND LOWER(".mysql_san($config['CC']['prefix'])."_balance.worldName) = '".mysql_san(strtolower($config['CC']['group']))."' LIMIT 1;";
  } else {
    $query = "UPDATE `".$config['table prefix']."Players` SET ".
             "`money` = `money` + ".((double)$amount)." ".
             "WHERE `uuid`='".mysql_san($playerUUID)."' LIMIT 1;";
  }
  $result = RunQuery($query, __file__, __line__);
  global $db;
  if(mysql_affected_rows($db) != 1) echo '<p>Failed to make payment to/from: '.$playerName.'!</p>';
}


}
?>