<?php

/**
 * A folder where to write the registrations
 */

$registrations_folder = "/var/taverna-registration";



function xmlescape($string){
          $res = str_replace("&", "&amp;",$string);
          $res = str_replace("<", "&lt;",$res);
          $res = str_replace(">", "&gt;",$res);
          return $res;
}



/* From http://php.net/manual/en/function.uiqid.php 
 * Requires yum install uuid-php
   and in .htaccess / php.ini:
     php_value allow_call_time_pass_reference true
 */

class uuid { 
    /** 
     * This class enables you to get real uuids using the OSSP library. 
     * Note you need php-uuid installed. 
     * On my system 1000 UUIDs are created in 0.0064 seconds. 
     * 
     * @author Marius Karthaus 
     * 
     */ 
    
    protected $uuidobject; 
    
    /** 
     * On long running deamons i've seen a lost resource. This checks the resource and creates it if needed. 
     * 
     */ 
    protected function create() { 
        if (! is_resource ( $this->uuidobject )) { 
            uuid_create ( &$this->uuidobject ); 
        } 
    } 
    
    /** 
     * Return a type 1 (MAC address and time based) uuid 
     * 
     * @return String 
     */ 
    public function v1() { 
        $this->create (); 
        uuid_make ( $this->uuidobject, UUID_MAKE_V1 ); 
        uuid_export ( $this->uuidobject, UUID_FMT_STR, &$uuidstring ); 
        return trim ( $uuidstring ); 
    } 
    
    /** 
     * Return a type 4 (random) uuid 
     * 
     * @return String 
     */ 
    public function v4() { 
        $this->create (); 
        uuid_make ( $this->uuidobject, UUID_MAKE_V4 ); 
        uuid_export ( $this->uuidobject, UUID_FMT_STR, &$uuidstring ); 
        return trim ( $uuidstring ); 
    } 
    
    /** 
     * Return a type 5 (SHA-1 hash) uuid 
     * 
     * @return String 
     */ 
    public function v5() { 
        $this->create (); 
        uuid_make ( $this->uuidobject, UUID_MAKE_V5 ); 
        uuid_export ( $this->uuidobject, UUID_FMT_STR, $uuidstring ); 
        return trim ( $uuidstring ); 
    } 
} 

	if(isset($_POST['taverna_registration'])){
		
		$taverna_version   = $_POST['taverna_version'];
		$first_name   = $_POST['first_name'];
		$last_name   = $_POST['last_name'];
		$email   = $_POST['email'];
		$keep_me_informed   = $_POST['keep_me_informed'];
		$institution_or_company   = $_POST['institution_or_company'];
		$industry   = $_POST['industry_type'];
		$field   = $_POST['field'];
		$purpose   = $_POST['purpose'];
		
		$uuid=new uuid(); 
         
		
		// Generate user registration data file name with a random identifier
		$random_id = $uuid->v4();
		$user_registration_file_name = $registrations_folder . "/user_registration_" . $random_id . ".xml";
		$user_registration_file = fopen($user_registration_file_name,'w') or die ("Could not open file ". $user_registration_file_name . " for writing." );
		
		// Save this to a file
		/*
		 $registration_data = "";
		$registration_data .= "Taverna version=" . $taverna_version . "\n";
		$registration_data .= "First name=" . $first_name . "\n";
		$registration_data .= "Last name=" . $last_name . "\n";
		$registration_data .= "Email address=" . $email . "\n";
		$registration_data .= "Keep me informed by email=" . $keep_me_informed . "\n";
		$registration_data .= "Institution or company=" . $institution_or_company . "\n";
		$registration_data .= "Industry=" . $industry_type . "\n";
		$registration_data .= "Field of interest=" . $field . "\n";
		$registration_data .= "Purpose of using Taverna=" . $purpose;
		 */
		
		$registration_data = "";
		$registration_data .= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		$registration_data .= "<registration_data>\n";
		$registration_data .= "\t<taverna_version>".xmlescape($taverna_version)."</taverna_version>\n";
		$registration_data .= "\t<first_name>".xmlescape($first_name)."</first_name>\n";
		$registration_data .= "\t<last_name>".xmlescape($last_name)."</last_name>\n";
		$registration_data .= "\t<email_address>".xmlescape($email)."</email_address>\n";
		$registration_data .= "\t<keep_me_informed>".xmlescape($keep_me_informed)."</keep_me_informed>\n";
		$registration_data .= "\t<institution_or_company_name>".xmlescape($institution_or_company)."</institution_or_company_name>\n";
		$registration_data .= "\t<industry_type>".xmlescape($industry)."</industry_type>\n";
		$registration_data .= "\t<field_of_interest>".xmlescape($field)."</field_of_interest>\n";
		$registration_data .= "\t<purpose_of_using_taverna>".xmlescape($purpose)."</purpose_of_using_taverna>\n";
		$registration_data .= "</registration_data>\n";
		
		fwrite($user_registration_file, $registration_data) or die ("Could not write to file ". $user_registration_file_name );
		fclose($user_registration_file);
		echo "Registration successful!";
	}
?>
