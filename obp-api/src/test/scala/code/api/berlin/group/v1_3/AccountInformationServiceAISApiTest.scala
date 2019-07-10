package code.api.berlin.group.v1_3

import code.api.ErrorMessage
import code.api.berlin.group.v1_3.JSONFactory_BERLIN_GROUP_1_3.{AccountBalancesV13, CardTransactionsJsonV13, ConsentStatusJsonV13, CoreAccountsJsonV13, GetConsentResponseJson, PostConsentResponseJson, TransactionsJsonV13}
import code.api.builder.AccountInformationServiceAISApi.APIMethods_AccountInformationServiceAISApi
import code.api.util.APIUtil.OAuth._
import code.api.util.ErrorMessages._
import code.setup.{APIResponse, DefaultUsers}
import com.github.dwickern.macros.NameOf.nameOf
import net.liftweb.json.Serialization.write
import org.scalatest.Tag

class AccountInformationServiceAISApiTest extends BerlinGroupServerSetupV1_3 with DefaultUsers {

  object getAccountList extends Tag(nameOf(APIMethods_AccountInformationServiceAISApi.getAccountList))

  object getBalances extends Tag(nameOf(APIMethods_AccountInformationServiceAISApi.getBalances))

  object getTransactionList extends Tag(nameOf(APIMethods_AccountInformationServiceAISApi.getTransactionList))

  object getCardAccountTransactionList extends Tag(nameOf(APIMethods_AccountInformationServiceAISApi.getCardAccountTransactionList))

  object createConsent extends Tag(nameOf(APIMethods_AccountInformationServiceAISApi.createConsent))

  object deleteConsent extends Tag(nameOf(APIMethods_AccountInformationServiceAISApi.deleteConsent))

  object getConsentInformation extends Tag(nameOf(APIMethods_AccountInformationServiceAISApi.getConsentInformation))

  object getConsentStatus extends Tag(nameOf(APIMethods_AccountInformationServiceAISApi.getConsentStatus))
  
  object startConsentAuthorisation extends Tag(nameOf(APIMethods_AccountInformationServiceAISApi.startConsentAuthorisation))

  object getConsentScaStatus extends Tag(nameOf(APIMethods_AccountInformationServiceAISApi.getConsentScaStatus))
  
  object updateConsentsPsuData extends Tag(nameOf(APIMethods_AccountInformationServiceAISApi.updateConsentsPsuData))

  object getConsentAuthorisation extends Tag(nameOf(APIMethods_AccountInformationServiceAISApi.getConsentAuthorisation))
  
  feature(s"BG v1.3 - $getAccountList") {
    scenario("Not Authentication User, test failed ", BerlinGroupV1_3, getAccountList) {
      val requestGet = (V1_3_BG / "accounts").GET
      val response = makeGetRequest(requestGet)

      Then("We should get a 400 ")
      response.code should equal(400)
      response.body.extract[ErrorMessage].message should startWith(UserNotLoggedIn)
    }

    scenario("Authentication User, test succeed", BerlinGroupV1_3, getAccountList) {
      val requestGet = (V1_3_BG / "accounts").GET <@ (user1)
      val response = makeGetRequest(requestGet)

      Then("We should get a 200 ")
      response.code should equal(200)
      response.body.extract[CoreAccountsJsonV13].accounts.length > 1 should be (true)
    }
  }

  feature(s"BG v1.3 - $getBalances") {
    scenario("Authentication User, test succeed", BerlinGroupV1_3, getBalances) {
      val testBankId = testAccountId1
      val requestGet = (V1_3_BG / "accounts" /testBankId.value/ "balances").GET <@ (user1)
      val response: APIResponse = makeGetRequest(requestGet)

      Then("We should get a 200 ")
      response.code should equal(200)
      response.body.extract[AccountBalancesV13].`balances`.length > 0 should be (true)
      response.body.extract[AccountBalancesV13].account.iban should be ("")
    }
  }  

  feature(s"BG v1.3 - $getTransactionList") {
    scenario("Authentication User, test succeed", BerlinGroupV1_3, getTransactionList) {
      val testBankId = testAccountId1
      val requestGet = (V1_3_BG / "accounts" /testBankId.value/ "transactions").GET <@ (user1)
      val response: APIResponse = makeGetRequest(requestGet)

      Then("We should get a 200 ")
      response.code should equal(200)
      response.body.extract[TransactionsJsonV13].account.iban should be ("")
      response.body.extract[TransactionsJsonV13].transactions.booked.length >0 should be (true)
      response.body.extract[TransactionsJsonV13].transactions.pending.length >0 should be (true)
    }
  }

  feature(s"BG v1.3 - $getCardAccountTransactionList") {
    scenario("Authentication User, test succeed", BerlinGroupV1_3, getCardAccountTransactionList) {
      val testBankId = testAccountId1
      val requestGet = (V1_3_BG / "card-accounts" /testBankId.value/ "transactions").GET <@ (user1)
      val response: APIResponse = makeGetRequest(requestGet)

      Then("We should get a 200 ")
      response.code should equal(200)
      response.body.extract[CardTransactionsJsonV13].cardAccount.maskedPan.length >0 should be (true)
      response.body.extract[CardTransactionsJsonV13].transactions.booked.length >0 should be (true)
    }
  }

  feature(s"BG v1.3 - $createConsent") {
    scenario("Authentication User, test succeed", BerlinGroupV1_3, createConsent) {
      val testBankId = testAccountId1
      val postJsonBody = APIMethods_AccountInformationServiceAISApi
        .resourceDocs
        .filter( _.partialFunction == APIMethods_AccountInformationServiceAISApi.createConsent)
        .head.exampleRequestBody.asInstanceOf[JvalueCaseClass] //All the Json String convert to JvalueCaseClass implicitly 
        .jvalueToCaseclass
      val requestPost = (V1_3_BG / "consents" ).POST <@ (user1)
      val response: APIResponse = makePostRequest(requestPost, write(postJsonBody))

      Then("We should get a 201 ")
      response.code should equal(201)
      response.body.extract[PostConsentResponseJson].consentId should not be (empty)
      response.body.extract[PostConsentResponseJson].consentStatus should be ("received")
    }
  }


  feature(s"BG v1.3 - $createConsent and $deleteConsent") {
    scenario("Authentication User, test succeed", BerlinGroupV1_3, createConsent) {
      val testBankId = testAccountId1
      val postJsonBody = APIMethods_AccountInformationServiceAISApi
        .resourceDocs
        .filter( _.partialFunction == APIMethods_AccountInformationServiceAISApi.createConsent)
        .head.exampleRequestBody.asInstanceOf[JvalueCaseClass] //All the Json String convert to JvalueCaseClass implicitly 
        .jvalueToCaseclass
      val requestPost = (V1_3_BG / "consents" ).POST <@ (user1)
      val response: APIResponse = makePostRequest(requestPost, write(postJsonBody))

      Then("We should get a 201 ")
      response.code should equal(201)
      response.body.extract[PostConsentResponseJson].consentId should not be (empty)

      val consentId =response.body.extract[PostConsentResponseJson].consentId

      Then("We test the delete consent ")  
      val requestDelete = (V1_3_BG / "consents"/consentId ).DELETE <@ (user1)
      val responseDelete = makeDeleteRequest(requestDelete)
      responseDelete.code should be (204)

      //TODO We can not delete one consent two time, will fix it later.
//      val responseDeleteSecondTime = makeDeleteRequest(requestDelete)
//      responseDeleteSecondTime.code should be (400)
    }
  }  

  feature(s"BG v1.3 - $createConsent and $getConsentInformation and $getConsentStatus") {
    scenario("Authentication User, test succeed", BerlinGroupV1_3, createConsent) {
      val testBankId = testAccountId1
      val postJsonBody = APIMethods_AccountInformationServiceAISApi
        .resourceDocs
        .filter( _.partialFunction == APIMethods_AccountInformationServiceAISApi.createConsent)
        .head.exampleRequestBody.asInstanceOf[JvalueCaseClass] //All the Json String convert to JvalueCaseClass implicitly 
        .jvalueToCaseclass
      val requestPost = (V1_3_BG / "consents" ).POST <@ (user1)
      val response: APIResponse = makePostRequest(requestPost, write(postJsonBody))

      Then("We should get a 201 ")
      response.code should equal(201)
      response.body.extract[PostConsentResponseJson].consentId should not be (empty)

      val consentId =response.body.extract[PostConsentResponseJson].consentId

      Then(s"We test the $getConsentInformation")
      val requestGet = (V1_3_BG / "consents"/consentId ).GET <@ (user1)
      val responseGet = makeGetRequest(requestGet)
      responseGet.code should be (200)
      responseGet.body.extract[GetConsentResponseJson].consentStatus should be ("received")

      Then(s"We test the $getConsentStatus")
      val requestGetStatus = (V1_3_BG / "consents"/consentId /"status" ).GET <@ (user1)
      val responseGetStatus = makeGetRequest(requestGetStatus)
      responseGetStatus.code should be (200)
      responseGetStatus.body.extract[ConsentStatusJsonV13].consentStatus should be ("received")
      
    }
  }

}