<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}


// Database config
function ConnectDB(){global $db,$config;


  $host     = 'localhost';
  $port     = 3306;
  $username = 'minecraft';
  $password = 'password123';
  $database = 'minecraft';
  $config['table prefix'] = 'WA_';


  $db=@mysql_pconnect($host.($port==0?'':':'.((int)$port)),$username,$password);
  if(!$db || !@mysql_select_db($database,$db)){echo '<p>MySQL Error: '.mysql_error().'</p>'; exit();}
  mysql_query("SET names UTF8");
}


// iConomy config
$config['iConomy']['use']   = 'false';    // ( true / false )  Do you want to use the iConomy table to get the money from (iConomy tables and WebAuction tables need to be in the same Database)
$config['iConomy']['table'] = 'iConomy'; // 'iConomy' is the default table name when using MySQL with iConomy

// CraftConomy config
$config['CC']['use']   = 'false';    // ( true / false )  Do you want to use the CraftConomy table to get the money from (CraftConomy tables and WebAuction tables need to be in the same Database)
$config['CC']['prefix'] = 'cc3'; // the thable prefix ot the CrafConomy tables
$config['CC']['group'] = 'default'; // the name of the money group we should use
$config['CC']['currency'] = 'Dollar'; // the name of the currency we should use

?>