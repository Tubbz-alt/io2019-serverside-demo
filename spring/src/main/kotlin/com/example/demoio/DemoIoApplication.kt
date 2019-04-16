package com.example.demoio

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.gcp.data.datastore.core.mapping.Entity
import org.springframework.cloud.gcp.data.datastore.repository.DatastoreRepository
import org.springframework.cloud.gcp.storage.GoogleStorageResource
import org.springframework.context.ApplicationContext
import org.springframework.core.io.Resource
import org.springframework.core.io.WritableResource
import org.springframework.data.annotation.Id
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletResponse
import javax.websocket.server.PathParam

@SpringBootApplication
class DemoIoApplication

fun main(args: Array<String>) {
    runApplication<DemoIoApplication>(*args)
}

@RestController
class UploadController(
        private val applicationContext: ApplicationContext,
        private val photoRepository: PhotoRepository
) {
    private val prefix = "gs://cloud-kotlin-io19/demo"

    @PostMapping("/upload")
    fun upload(@RequestParam("file") file : MultipartFile): Photo {
        val id = UUID.randomUUID().toString()
        val fileUri = "$prefix/$id"

        val resource = applicationContext.getResource(fileUri) as GoogleStorageResource

        resource.outputStream.use { out ->
            file.inputStream.use {
                it.copyTo(out)
            }
        }

        return photoRepository.save(Photo(
                id = id,
                uri = "/image/$id"
        ))
    }

    @GetMapping("/image/{id}")
    fun image(@PathVariable id : String) : ResponseEntity<Resource> {
        val resource = applicationContext.getResource("$prefix/$id")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_OCTET_STREAM

        return ResponseEntity(resource, headers, HttpStatus.OK)
    }

    @DeleteMapping("/image/{id}")
    fun deleteImage(@PathVariable id : String) : ResponseEntity<String> {
        val resource = applicationContext.getResource("$prefix/$id") as GoogleStorageResource
        return if (resource.exists()) {
            photoRepository.deleteById(id)
            resource.blob.delete()

            ResponseEntity(HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }
}

@RepositoryRestResource
interface PhotoRepository : DatastoreRepository<Photo, String>

@Entity
data class Photo (
        @Id
        var id: String? = null,
        var uri: String? = null,
        var label: String? = null
)
