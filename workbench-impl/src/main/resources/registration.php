<?php
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
		
		// Generate user registration data file name with a random identifier
		$random_id = mt_rand();
		$user_registration_file_name = "user_registration_" . $random_id . ".xml";
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
		$registration_data .= "\t<taverna_version>".$taverna_version."</taverna_version>\n";
		$registration_data .= "\t<first_name>".$first_name."</first_name>\n";
		$registration_data .= "\t<last_name>".$last_name."</last_name>\n";
		$registration_data .= "\t<email_address>".$email."</email_address>\n";
		$registration_data .= "\t<keep_me_informed>".$keep_me_informed."</keep_me_informed>\n";
		$registration_data .= "\t<institution_or_company_name>".$institution_or_company."</institution_or_company_name>\n";
		$registration_data .= "\t<industry_type>".$industry."</industry_type>\n";
		$registration_data .= "\t<field_of_interest>".$field."</field_of_interest>\n";
		$registration_data .= "\t<purpose_of_using_taverna>".$purpose."</purpose_of_using_taverna>\n";
		$registration_data .= "</registration_data>";
		
		fwrite($user_registration_file, $registration_data) or die ("Could not write to file ". $user_registration_file_name );
		fclose($user_registration_file);
		echo "Registration successful!";
	}
?>