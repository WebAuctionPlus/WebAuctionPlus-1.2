<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
//global $config,$html,$user;
$outputs=array();


// item display block
$outputs['item']='
<div style="padding-top: 10px; padding-bottom: 10px; text-align: center;">
<img src="{item image url}" alt="{item title}" style="width: 32px; height: 32px; margin-bottom: 5px;" /><br />

<b>{item name}</b>
{has damage}<br />{item damage}{/has damage}
{has custom name}<br />{custom name}{/has custom name}
{has lore}<br />{lore}{/has lore}
{has enchantments}<br />{enchantments}{/has enchantments}

<br />
</div>
';


// custom name
$outputs['custom name']=
'<font size="-1" class="display_custom_name"><b>{name}</b></font>
';


// lore
$outputs['lore line']=
'<font size="-1" class="display_lore"><i>{line}</i></font>
';
$outputs['lore split']=
'<br />';


// enchantment
$outputs['enchantment']=
'<font size="-1">{ench title} {ench level}</font>
';
$outputs['enchantment split']=
'<br />';


return($outputs);
?>