/** 
 * Created by: Dhem.
 * Created on: September 10 2024.
 * Description: This function will check if the Gig Stage is "Contract Pending" and ensure that the existing Gig Event Date and Band Desired do not already exist in the record.
**/
// -----------------------------------------
// Global variable
// -----------------------------------------
dealsModule = "Deals";
contractPendingStage = "Contract Pending";
holdStage = "Hold Challenge Contract Pending";
// -----------------------------------------
// email template IDs
// -----------------------------------------
templateId = "4354338000146712006";
templateId2 = "4354338000146684609";
// -----------------------------------------
// questionnaire template ID
// -----------------------------------------
questionnaire1Id = "4354338000023300185";
questionnaire2Id = "4354338000023300054";
questionnaire3Id = "4354338000023300060";
questionnaire4Id = "4354338000022170505";
questionnaire5Id = "4354338000023300389";
questionnaire6Id = "4354338000023482235";
questionnaire7Id = "4354338000021605791";
questionnaire8Id = "4354338000019201386";
// 
// Static Signature
staticSignature = "<div>Warm regards,<br><br>Jordan Kahn Music Company<br>c. <a href=\"tel:+9727938267\">(844) 800-7278</a><br><a href=\"https://www.instagram.com/jordankahnorchestra/\">Kahn Music Company Instagram</a></div>";
// Static Email
Jordan = "jordan@jordankahnmusiccompany.com";
// 
gigRecord = zoho.crm.getRecordById(dealsModule,gigId);
info gigRecord;
// -----------------------------------------
// Get Gig record specific fields value
// -----------------------------------------
if(gigRecord.size() > 0)
{
	gigOwner = gigRecord.get("Owner").getJSON("name");
	gigName = gigRecord.getJSON("Deal_Name");
	gigstage = gigRecord.getJSON("Stage");
	gigDate = gigRecord.get("Gig_Event_Dates").toDate();
	bandDesired = gigRecord.getJSON("Band_Desired");
	if(!gigRecord.getJSON("Contact_Name").isNull())
	{
		contactName = gigRecord.getJSON("Contact_Name").getJSON("name");
	}
	else
	{
		contactName = "";
	}
	questionnaireEmail = gigRecord.get("Questionnaire_Email_Templates");
	formattedBandName = ifNull(gigRecord.get("Formatted_Band_Name"),"");
	typeOfEvent = ifNull(gigRecord.get("Type_of_Event"),"");
}
// -----------------------------------------
// Search records based on Contract Pending Stage
// -----------------------------------------
searchGigREecords = {"select_query":"SELECT id, Deal_Name, Band_Desired, Gig_Event_Dates, Stage FROM Deals WHERE (((Stage='" + contractPendingStage + "') AND (Band_Desired='" + gigRecord.get("Band_Desired") + "')) AND Gig_Event_Dates='" + gigRecord.get("Gig_Event_Dates") + "') LIMIT 500"};
// -----------------------------------------
// Search records based on Hold Stage 
// -----------------------------------------
searchGigInHoldStage = {"select_query":"SELECT id,Deal_Name,Band_Desired, Gig_Event_Dates, Stage FROM Deals WHERE (((Stage='" + holdStage + "') AND (Band_Desired='" + gigRecord.get("Band_Desired") + "')) AND Gig_Event_Dates='" + gigRecord.get("Gig_Event_Dates") + "') LIMIT 500"};
headermp = Map();
headermp.put("Content-Type","application/json");
searchGigREecordsResponse = invokeurl
[
	url :"https://www.zohoapis.com/crm/v6/coql"
	type :POST
	parameters:searchGigREecords.toString()
	headers:headermp
	connection:"zapp_conn"
];
info searchGigREecordsResponse;
searchGigInHoldStageResponse = invokeurl
[
	url :"https://www.zohoapis.com/crm/v6/coql"
	type :POST
	parameters:searchGigInHoldStage.toString()
	headers:headermp
	connection:"zapp_conn"
];
info searchGigInHoldStageResponse;
// -----------------------------------------
// Sending email to salesperson
// -----------------------------------------
info searchGigREecordsResponse.get("data").size() + " - Contract Pending";
info searchGigInHoldStageResponse.get("data").size() + " - Hold Challenge Contract Pending";
info "-----------------------------------------------------";
// info searchGigREecords;
info "-----------------------------------------------------";
// info searchGigInHoldStage;
notCurrentGigRecordWithContractPendingStageIsPresent = false;
for each  gigRecord in searchGigREecordsResponse.get("data")
{
	if(gigId != gigRecord.get("id").toLong())
	{
		info "searchGigREecords true";
		notCurrentGigRecordWithContractPendingStageIsPresent = true;
	}
}
notCurrentGigRecordWithHoldChallengeContractPendingStageIsPresent = false;
for each  gigRecord in searchGigInHoldStageResponse.get("data")
{
	if(gigId != gigRecord.get("id").toLong())
	{
		info "searchGigInHoldStage true";
		notCurrentGigRecordWithHoldChallengeContractPendingStageIsPresent = true;
	}
}
if(gigstage == contractPendingStage && notCurrentGigRecordWithContractPendingStageIsPresent)
{
	info "------IF-------";
	gigRecordList = "<ul>";
	// 	gigRecordList = list();
	for each  gigRecords in searchGigREecordsResponse.get("data")
	{
		gigRecordEntryId = gigRecords.get("id");
		if(gigRecordEntryId.toLong() != gigId)
		{
			gigRecordList = gigRecordList + "<li><a href='https://crm.zoho.com/crm/org706117641/tab/Potentials/" + gigRecordEntryId + "'>" + gigRecords.getJSON("Deal_Name") + "</a></li><br>";
		}
	}
	gigRecordList = gigRecordList + "</ul>";
	// 	
	fetch_email_template = invokeurl
	[
		url :"https://www.zohoapis.com/crm/v6/settings/email_templates/" + templateId
		type :GET
		connection:"zohocrm_templates"
	];
	if(fetch_email_template != "")
	{
		salesperson_subject = fetch_email_template.getJSON("email_templates").getJSON("subject");
		salesperson_subject = replaceAll(salesperson_subject,"gig_event_date",gigDate);
		// 		
		salesperson_email_body = "";
		salesperson_email_body = fetch_email_template.getJSON("email_templates").getJSON("content");
		salesperson_email_body = replaceAll(salesperson_email_body,"Gig_Owner",gigOwner);
		salesperson_email_body = replaceAll(salesperson_email_body,"Gig_Stage",contractPendingStage);
		salesperson_email_body = replaceAll(salesperson_email_body,"Gig_Name",gigName);
		salesperson_email_body = replaceAll(salesperson_email_body,"gig_event_date",gigDate);
		salesperson_email_body = replaceAll(salesperson_email_body,"band_desired",bandDesired);
		salesperson_email_body = replaceAll(salesperson_email_body,"Access_Here","<a href=\"https://crm.zoho.com/crm/org706117641/tab/Potentials/" + gigId + "\" title=\"Access here\" target=\"_blank\" rel=\"noopener noreferrer\" style=\"text-decoration:none\">here</a>");
		salesperson_email_body = replaceAll(salesperson_email_body,"GIG","<br>" + gigRecordList);
		sendmail
		[
			from :zoho.adminuserid
			to :salesPerson
			subject :salesperson_subject
			message :salesperson_email_body
		]
	}
}
else if(gigstage == holdStage && notCurrentGigRecordWithHoldChallengeContractPendingStageIsPresent)
{
	info "------ELSE IF-------";
	// -----------------------------------------
	// Sending email to salesperson
	// -----------------------------------------
	fetch_email_template = invokeurl
	[
		url :"https://www.zohoapis.com/crm/v6/settings/email_templates/" + templateId2
		type :GET
		connection:"zohocrm_templates"
	];
	if(fetch_email_template != "")
	{
		salesperson_subject = fetch_email_template.getJSON("email_templates").getJSON("subject");
		salesperson_subject = replaceAll(salesperson_subject,"Gig_Name",gigName);
		salesperson_subject = replaceAll(salesperson_subject,"Gig_Stage",gigstage);
		// 		
		salesperson_email_body = "";
		salesperson_email_body = fetch_email_template.getJSON("email_templates").getJSON("content");
		salesperson_email_body = replaceAll(salesperson_email_body,"Gig_Stage",gigstage);
		salesperson_email_body = replaceAll(salesperson_email_body,"Gig_Name",gigName);
		salesperson_email_body = replaceAll(salesperson_email_body,"Contact",contactName);
		salesperson_email_body = replaceAll(salesperson_email_body,"Access_Here","<a href=\"https://crm.zoho.com/crm/org706117641/tab/Potentials/" + gigId + "\" title=\"Access here\" target=\"_blank\" rel=\"noopener noreferrer\" style=\"text-decoration:none\">here</a>");
		sendmail
		[
			from :zoho.adminuserid
			to :salesPerson,Jordan
			subject :salesperson_subject
			message :salesperson_email_body
		]
	}
	// -----------------------------------------
	// Sending email Questionnaire to Client with Hold Stage
	// -----------------------------------------
	if(questionnaireEmail == "Questionnaire - Corporate/Social/Gala - new" && questionnaire1Id == "4354338000023300185")
	{
		fetch_email_template = invokeurl
		[
			url :"https://www.zohoapis.com/crm/v6/settings/email_templates/" + questionnaire1Id
			type :GET
			connection:"zohocrm_templates"
		];
		info "Email Template 1";
		salesperson_subject = fetch_email_template.getJSON("email_templates").getJSON("subject");
		emailSubject = replaceAll(salesperson_subject,"\$\{!Deals.Deal_Name\}",gigName);
		// 		
		fetch_email_content = fetch_email_template.getJSON("email_templates").get(0).getJSON("content");
		emailcontent = replaceAll(fetch_email_content,"\$\{!Deals.Contact_Name.First_Name\}",contactName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Formatted_Band_Name\}",formattedBandName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Type_of_Event\}",typeOfEvent);
		emailcontent = replaceAll(emailcontent,"\$\{!userSignature\}",staticSignature);
		// 		
		sendmail
		[
			from :zoho.adminuserid
			to :contactPerson
			subject :emailSubject
			message :emailcontent
		]
	}
	else if(questionnaireEmail == "Questionnaire - Straight - Planner edition - new" && questionnaire2Id == "4354338000023300054")
	{
		fetch_email_template = invokeurl
		[
			url :"https://www.zohoapis.com/crm/v6/settings/email_templates/" + questionnaire2Id
			type :GET
			connection:"zohocrm_templates"
		];
		info "Email Template 2";
		salesperson_subject = fetch_email_template.getJSON("email_templates").getJSON("subject");
		emailSubject = replaceAll(salesperson_subject,"\$\{!Deals.Deal_Name\}",gigName);
		// 		
		fetch_email_content = fetch_email_template.getJSON("email_templates").get(0).getJSON("content");
		emailcontent = replaceAll(fetch_email_content,"\$\{!Deals.Contact_Name.First_Name\}",contactName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Formatted_Band_Name\}",formattedBandName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Type_of_Event\}",typeOfEvent);
		emailcontent = replaceAll(emailcontent,"\$\{!userSignature\}",staticSignature);
		// 		
		sendmail
		[
			from :zoho.adminuserid
			to :contactPerson
			subject :emailSubject
			message :emailcontent
		]
	}
	else if(questionnaireEmail == "Questionnaire - Straight - Client edition - new" && questionnaire3Id == "4354338000023300060")
	{
		fetch_email_template = invokeurl
		[
			url :"https://www.zohoapis.com/crm/v6/settings/email_templates/" + questionnaire3Id
			type :GET
			connection:"zohocrm_templates"
		];
		info "Email Template 3";
		salesperson_subject = fetch_email_template.getJSON("email_templates").getJSON("subject");
		emailSubject = replaceAll(salesperson_subject,"\$\{!Deals.Deal_Name\}",gigName);
		// 		
		fetch_email_content = fetch_email_template.getJSON("email_templates").get(0).getJSON("content");
		emailcontent = replaceAll(fetch_email_content,"\$\{!Deals.Contact_Name.First_Name\}",contactName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Formatted_Band_Name\}",formattedBandName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Type_of_Event\}",typeOfEvent);
		emailcontent = replaceAll(emailcontent,"\$\{!userSignature\}",staticSignature);
		// 		
		sendmail
		[
			from :zoho.adminuserid
			to :contactPerson
			subject :emailSubject
			message :emailcontent
		]
	}
	else if(questionnaireEmail == "Questionnaire - Straight - Planner edition" && questionnaire4Id == "4354338000022170505")
	{
		fetch_email_template = invokeurl
		[
			url :"https://www.zohoapis.com/crm/v6/settings/email_templates/" + questionnaire4Id
			type :GET
			connection:"zohocrm_templates"
		];
		info "Email Template 4";
		salesperson_subject = fetch_email_template.getJSON("email_templates").getJSON("subject");
		emailSubject = replaceAll(salesperson_subject,"\$\{!Deals.Deal_Name\}",gigName);
		// 		
		fetch_email_content = fetch_email_template.getJSON("email_templates").get(0).getJSON("content");
		emailcontent = replaceAll(fetch_email_content,"\$\{!Deals.Contact_Name.First_Name\}",contactName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Formatted_Band_Name\}",formattedBandName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Type_of_Event\}",typeOfEvent);
		emailcontent = replaceAll(emailcontent,"\$\{!userSignature\}",staticSignature);
		// 		
		sendmail
		[
			from :zoho.adminuserid
			to :contactPerson
			subject :emailSubject
			message :emailcontent
		]
	}
	else if(questionnaireEmail == "Questionnaire - Gay - new" && questionnaire5Id == "4354338000023300389")
	{
		fetch_email_template = invokeurl
		[
			url :"https://www.zohoapis.com/crm/v6/settings/email_templates/" + questionnaire5Id
			type :GET
			connection:"zohocrm_templates"
		];
		info "Email Template 5";
		salesperson_subject = fetch_email_template.getJSON("email_templates").getJSON("subject");
		emailSubject = replaceAll(salesperson_subject,"\$\{!Deals.Deal_Name\}",gigName);
		// 		
		fetch_email_content = fetch_email_template.getJSON("email_templates").get(0).getJSON("content");
		emailcontent = replaceAll(fetch_email_content,"\$\{!Deals.Contact_Name.First_Name\}",contactName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Formatted_Band_Name\}",formattedBandName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Type_of_Event\}",typeOfEvent);
		emailcontent = replaceAll(emailcontent,"\$\{!userSignature\}",staticSignature);
		// 		
		sendmail
		[
			from :zoho.adminuserid
			to :contactPerson
			subject :emailSubject
			message :emailcontent
		]
	}
	else if(questionnaireEmail == "Questionnaire explanation - Man - planner edition" && questionnaire6Id == "4354338000023482235")
	{
		fetch_email_template = invokeurl
		[
			url :"https://www.zohoapis.com/crm/v6/settings/email_templates/" + questionnaire6Id
			type :GET
			connection:"zohocrm_templates"
		];
		info "Email Template 6";
		salesperson_subject = fetch_email_template.getJSON("email_templates").getJSON("subject");
		emailSubject = replaceAll(salesperson_subject,"\$\{!Deals.Band_Desired\}",bandDesired);
		// 		
		fetch_email_content = fetch_email_template.getJSON("email_templates").get(0).getJSON("content");
		emailcontent = replaceAll(fetch_email_content,"\$\{!Deals.Contact_Name.First_Name\}",contactName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Formatted_Band_Name\}",formattedBandName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Type_of_Event\}",typeOfEvent);
		emailcontent = replaceAll(emailcontent,"\$\{!userSignature\}",staticSignature);
		// 		
		sendmail
		[
			from :zoho.adminuserid
			to :contactPerson
			subject :emailSubject
			message :emailcontent
		]
	}
	else if(questionnaireEmail == "Questionnaire explanation - Man - client edition" && questionnaire7Id == "4354338000021605791")
	{
		fetch_email_template = invokeurl
		[
			url :"https://www.zohoapis.com/crm/v6/settings/email_templates/" + questionnaire7Id
			type :GET
			connection:"zohocrm_templates"
		];
		info "Email Template 7";
		salesperson_subject = fetch_email_template.getJSON("email_templates").getJSON("subject");
		emailSubject = replaceAll(salesperson_subject,"\$\{!Deals.Band_Desired\}",bandDesired);
		// 		
		fetch_email_content = fetch_email_template.getJSON("email_templates").get(0).getJSON("content");
		emailcontent = replaceAll(fetch_email_content,"\$\{!Deals.Contact_Name.First_Name\}",contactName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Formatted_Band_Name\}",formattedBandName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Type_of_Event\}",typeOfEvent);
		emailcontent = replaceAll(emailcontent,"\$\{!userSignature\}",staticSignature);
		// 		
		sendmail
		[
			from :zoho.adminuserid
			to :contactPerson
			subject :emailSubject
			message :emailcontent
		]
	}
	else if(questionnaireEmail == "Questionnaire explanation - Luxe" && questionnaire8Id == "4354338000019201386")
	{
		fetch_email_template = invokeurl
		[
			url :"https://www.zohoapis.com/crm/v6/settings/email_templates/" + questionnaire8Id
			type :GET
			connection:"zohocrm_templates"
		];
		info "Email Template 8";
		salesperson_subject = fetch_email_template.getJSON("email_templates").getJSON("subject");
		emailSubject = replaceAll(salesperson_subject,"\$\{!Deals.Band_Desired\}",bandDesired);
		// 		
		fetch_email_content = fetch_email_template.getJSON("email_templates").get(0).getJSON("content");
		emailcontent = replaceAll(fetch_email_content,"\$\{!Deals.Contact_Name.First_Name\}",contactName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Formatted_Band_Name\}",formattedBandName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Type_of_Event\}",typeOfEvent);
		emailcontent = replaceAll(emailcontent,"\$\{!userSignature\}",staticSignature);
		// 		
		sendmail
		[
			from :zoho.adminuserid
			to :contactPerson
			subject :emailSubject
			message :emailcontent
		]
	}
}
else
{
	info "------ELSE-------";
	// -----------------------------------------
	// Sending email Questionnaire to Client if there is no conflict
	// -----------------------------------------
	if(questionnaireEmail == "Questionnaire - Corporate/Social/Gala - new" && questionnaire1Id == "4354338000023300185")
	{
		fetch_email_template = invokeurl
		[
			url :"https://www.zohoapis.com/crm/v6/settings/email_templates/" + questionnaire1Id
			type :GET
			connection:"zohocrm_templates"
		];
		info "Email Template 1";
		salesperson_subject = fetch_email_template.getJSON("email_templates").getJSON("subject");
		emailSubject = replaceAll(salesperson_subject,"\$\{!Deals.Deal_Name\}",gigName);
		// 		
		fetch_email_content = fetch_email_template.getJSON("email_templates").get(0).getJSON("content");
		emailcontent = replaceAll(fetch_email_content,"\$\{!Deals.Contact_Name.First_Name\}",contactName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Formatted_Band_Name\}",formattedBandName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Type_of_Event\}",typeOfEvent);
		emailcontent = replaceAll(emailcontent,"\$\{!userSignature\}",staticSignature);
		// 		
		sendmail
		[
			from :zoho.adminuserid
			to :contactPerson
			subject :emailSubject
			message :emailcontent
		]
	}
	else if(questionnaireEmail == "Questionnaire - Straight - Planner edition - new" && questionnaire2Id == "4354338000023300054")
	{
		fetch_email_template = invokeurl
		[
			url :"https://www.zohoapis.com/crm/v6/settings/email_templates/" + questionnaire2Id
			type :GET
			connection:"zohocrm_templates"
		];
		info "Email Template 2";
		salesperson_subject = fetch_email_template.getJSON("email_templates").getJSON("subject");
		emailSubject = replaceAll(salesperson_subject,"\$\{!Deals.Deal_Name\}",gigName);
		// 		
		fetch_email_content = fetch_email_template.getJSON("email_templates").get(0).getJSON("content");
		emailcontent = replaceAll(fetch_email_content,"\$\{!Deals.Contact_Name.First_Name\}",contactName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Formatted_Band_Name\}",formattedBandName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Type_of_Event\}",typeOfEvent);
		emailcontent = replaceAll(emailcontent,"\$\{!userSignature\}",staticSignature);
		// 		
		sendmail
		[
			from :zoho.adminuserid
			to :contactPerson
			subject :emailSubject
			message :emailcontent
		]
	}
	else if(questionnaireEmail == "Questionnaire - Straight - Client edition - new" && questionnaire3Id == "4354338000023300060")
	{
		fetch_email_template = invokeurl
		[
			url :"https://www.zohoapis.com/crm/v6/settings/email_templates/" + questionnaire3Id
			type :GET
			connection:"zohocrm_templates"
		];
		info "Email Template 3";
		salesperson_subject = fetch_email_template.getJSON("email_templates").getJSON("subject");
		emailSubject = replaceAll(salesperson_subject,"\$\{!Deals.Deal_Name\}",gigName);
		// 		
		fetch_email_content = fetch_email_template.getJSON("email_templates").get(0).getJSON("content");
		emailcontent = replaceAll(fetch_email_content,"\$\{!Deals.Contact_Name.First_Name\}",contactName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Formatted_Band_Name\}",formattedBandName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Type_of_Event\}",typeOfEvent);
		emailcontent = replaceAll(emailcontent,"\$\{!userSignature\}",staticSignature);
		// 		
		sendmail
		[
			from :zoho.adminuserid
			to :contactPerson
			subject :emailSubject
			message :emailcontent
		]
	}
	else if(questionnaireEmail == "Questionnaire - Straight - Planner edition" && questionnaire4Id == "4354338000022170505")
	{
		fetch_email_template = invokeurl
		[
			url :"https://www.zohoapis.com/crm/v6/settings/email_templates/" + questionnaire4Id
			type :GET
			connection:"zohocrm_templates"
		];
		info "Email Template 4";
		salesperson_subject = fetch_email_template.getJSON("email_templates").getJSON("subject");
		emailSubject = replaceAll(salesperson_subject,"\$\{!Deals.Deal_Name\}",gigName);
		// 		
		fetch_email_content = fetch_email_template.getJSON("email_templates").get(0).getJSON("content");
		emailcontent = replaceAll(fetch_email_content,"\$\{!Deals.Contact_Name.First_Name\}",contactName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Formatted_Band_Name\}",formattedBandName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Type_of_Event\}",typeOfEvent);
		emailcontent = replaceAll(emailcontent,"\$\{!userSignature\}",staticSignature);
		// 		
		sendmail
		[
			from :zoho.adminuserid
			to :contactPerson
			subject :emailSubject
			message :emailcontent
		]
	}
	else if(questionnaireEmail == "Questionnaire - Gay - new" && questionnaire5Id == "4354338000023300389")
	{
		fetch_email_template = invokeurl
		[
			url :"https://www.zohoapis.com/crm/v6/settings/email_templates/" + questionnaire5Id
			type :GET
			connection:"zohocrm_templates"
		];
		info "Email Template 5";
		salesperson_subject = fetch_email_template.getJSON("email_templates").getJSON("subject");
		emailSubject = replaceAll(salesperson_subject,"\$\{!Deals.Deal_Name\}",gigName);
		// 		
		fetch_email_content = fetch_email_template.getJSON("email_templates").get(0).getJSON("content");
		emailcontent = replaceAll(fetch_email_content,"\$\{!Deals.Contact_Name.First_Name\}",contactName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Formatted_Band_Name\}",formattedBandName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Type_of_Event\}",typeOfEvent);
		emailcontent = replaceAll(emailcontent,"\$\{!userSignature\}",staticSignature);
		// 		
		sendmail
		[
			from :zoho.adminuserid
			to :contactPerson
			subject :emailSubject
			message :emailcontent
		]
	}
	else if(questionnaireEmail == "Questionnaire explanation - Man - planner edition" && questionnaire6Id == "4354338000023482235")
	{
		fetch_email_template = invokeurl
		[
			url :"https://www.zohoapis.com/crm/v6/settings/email_templates/" + questionnaire6Id
			type :GET
			connection:"zohocrm_templates"
		];
		info "Email Template 6";
		salesperson_subject = fetch_email_template.getJSON("email_templates").getJSON("subject");
		emailSubject = replaceAll(salesperson_subject,"\$\{!Deals.Band_Desired\}",bandDesired);
		// 		
		fetch_email_content = fetch_email_template.getJSON("email_templates").get(0).getJSON("content");
		emailcontent = replaceAll(fetch_email_content,"\$\{!Deals.Contact_Name.First_Name\}",contactName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Formatted_Band_Name\}",formattedBandName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Type_of_Event\}",typeOfEvent);
		emailcontent = replaceAll(emailcontent,"\$\{!userSignature\}",staticSignature);
		// 		
		sendmail
		[
			from :zoho.adminuserid
			to :contactPerson
			subject :emailSubject
			message :emailcontent
		]
	}
	else if(questionnaireEmail == "Questionnaire explanation - Man - client edition" && questionnaire7Id == "4354338000021605791")
	{
		fetch_email_template = invokeurl
		[
			url :"https://www.zohoapis.com/crm/v6/settings/email_templates/" + questionnaire7Id
			type :GET
			connection:"zohocrm_templates"
		];
		info "Email Template 7";
		salesperson_subject = fetch_email_template.getJSON("email_templates").getJSON("subject");
		emailSubject = replaceAll(salesperson_subject,"\$\{!Deals.Band_Desired\}",bandDesired);
		// 		
		fetch_email_content = fetch_email_template.getJSON("email_templates").get(0).getJSON("content");
		emailcontent = replaceAll(fetch_email_content,"\$\{!Deals.Contact_Name.First_Name\}",contactName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Formatted_Band_Name\}",formattedBandName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Type_of_Event\}",typeOfEvent);
		emailcontent = replaceAll(emailcontent,"\$\{!userSignature\}",staticSignature);
		// 		
		sendmail
		[
			from :zoho.adminuserid
			to :contactPerson
			subject :emailSubject
			message :emailcontent
		]
	}
	else if(questionnaireEmail == "Questionnaire explanation - Luxe" && questionnaire8Id == "4354338000019201386")
	{
		fetch_email_template = invokeurl
		[
			url :"https://www.zohoapis.com/crm/v6/settings/email_templates/" + questionnaire8Id
			type :GET
			connection:"zohocrm_templates"
		];
		info "Email Template 8";
		salesperson_subject = fetch_email_template.getJSON("email_templates").getJSON("subject");
		emailSubject = replaceAll(salesperson_subject,"\$\{!Deals.Band_Desired\}",bandDesired);
		// 		
		fetch_email_content = fetch_email_template.getJSON("email_templates").get(0).getJSON("content");
		emailcontent = replaceAll(fetch_email_content,"\$\{!Deals.Contact_Name.First_Name\}",contactName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Formatted_Band_Name\}",formattedBandName);
		emailcontent = replaceAll(emailcontent,"\$\{!Deals.Type_of_Event\}",typeOfEvent);
		emailcontent = replaceAll(emailcontent,"\$\{!userSignature\}",staticSignature);
		// 		
		sendmail
		[
			from :zoho.adminuserid
			to :contactPerson
			subject :emailSubject
			message :emailcontent
		]
	}
	zoho.crm.updateRecord("Deals",gigId,{"Trigger_Hold_Stage":false},{"trigger":{"workflow"}});
	zoho.crm.updateRecord("Deals",gigId,{"Trigger_Hold_Stage":true},{"trigger":{"workflow"}});
}