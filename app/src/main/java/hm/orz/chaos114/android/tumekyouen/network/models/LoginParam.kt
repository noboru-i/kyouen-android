/**
* Kyouen API
* Kyouen server's API.
*
* The version of the OpenAPI document: 0.0.1
* 
*
* NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
* https://openapi-generator.tech
* Do not edit the class manually.
*/
package hm.orz.chaos114.android.tumekyouen.network.models


import com.squareup.moshi.Json
/**
 * 
 * @param token 
 * @param tokenSecret 
 */

data class LoginParam (
    @Json(name = "token")
    val token: kotlin.String,
    @Json(name = "token_secret")
    val tokenSecret: kotlin.String
)

