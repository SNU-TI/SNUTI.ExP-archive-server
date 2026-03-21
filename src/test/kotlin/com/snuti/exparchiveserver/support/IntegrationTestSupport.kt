package com.snuti.exparchiveserver.support


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import tools.jackson.databind.ObjectMapper

abstract class IntegrationTestSupport {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper
}