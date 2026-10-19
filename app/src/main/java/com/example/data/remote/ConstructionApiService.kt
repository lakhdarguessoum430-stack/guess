package com.example.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ConstructionApiService {

    @GET("api/projects")
    suspend fun getProjects(): Response<List<ProjectDto>>

    @POST("api/projects")
    suspend fun createProject(@Body project: ProjectDto): Response<ProjectDto>
}
