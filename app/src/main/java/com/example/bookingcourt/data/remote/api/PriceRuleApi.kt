//package com.example.bookingcourt.data.remote.api
//
//import com.example.bookingcourt.data.remote.dto.ApiResponse
//import com.example.bookingcourt.data.remote.dto.PriceRuleDto
//import com.example.bookingcourt.data.remote.dto.UpdatePriceRuleRequest
//import retrofit2.Response
//import retrofit2.http.*
//
//interface PriceRuleApi {
//    /**
//     * GET /api/pricerules
//     * Response: ApiResponse<List<PriceRuleDto>>
//     */
//    @GET("pricerules")
//    suspend fun getAllPriceRules(): Response<ApiResponse<List<PriceRuleDto>>>>
//
//    /**
//     * GET /api/pricerules/venue/{venueId}
//     * Response: List<PriceRuleDto> (không bọc)
//     */
//    @GET("pricerules/venue/{venueId}")
//    suspend fun getPriceRulesByVenue(
//        @Path("venueId") venueId: Long
//    ): Response<List<PriceRuleDto>>
//
//    /**
//     * PUT /api/pricerules/{id}
//     * Response: PriceRuleDto (không bọc)
//     */
//    @PUT("pricerules/{id}")
//    suspend fun updatePriceRule(
//        @Path("id") priceRuleId: Long,
//        @Body request: UpdatePriceRuleRequest
//    ): Response<PriceRuleDto>
//
//    /**
//     * PATCH /api/pricerules/{id}/toggle
//     * Response: PriceRuleDto (không bọc)
//     */
//    @PATCH("pricerules/{id}/toggle")
//    suspend fun togglePriceRule(
//        @Path("id") priceRuleId: Long
//    ): Response<PriceRuleDto>
//}
//
