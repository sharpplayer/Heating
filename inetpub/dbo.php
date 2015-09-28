<?php

  require_once('view.php');

  class CDatabaseObject extends CDatabaseView
  {
    var $m_nDBO_TYPE_USER = 0;
    var $m_nDBO_TYPE_SYSTEM = 1;
    var $m_nDBO_TYPE_MIXED = 2;

    function GetTableType(){ return($this->m_nDBO_TYPE_MIXED);}
    function Insert($p_oSession){}
    function Delete($p_oSession){}
    function Update($p_oSession, $p_oFilter = ""){}
    function Synchronise($p_oSession1, $p_oSession2){}
    function Upgrade($p_oSession,$p_sCurrentVersion){return(true);}
    function GetVersion(){ return("1.00");}
  }
?>