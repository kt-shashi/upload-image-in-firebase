package com.shashi.uploadimagetofirebasestorage

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.shashi.uploadimagetofirebasestorage.databinding.ActivityMainBinding
import java.io.InputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    lateinit var filepath: Uri
    lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "FirebaseStorage"

        initViews()
    }

    /*  Steps:
        1. Check for permission
        2. Create Intent
        3. Receive the result image and convert
            the url into bitmap
        4. Upload the image using uri
     */

    private fun initViews() {
        binding.imageViewProfile.setOnClickListener { imageViewClicked() }
        binding.buttonUpload.setOnClickListener { buttonUploadClicked() }
    }

    private fun imageViewClicked() {
        permissionCheck()
    }

    private fun permissionCheck() {

        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                        createIntent()

                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    }

                    override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, token: PermissionToken?) {
                        token?.continuePermissionRequest()
                    }

                }).check()

    }

    private fun createIntent() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Select image to upload"), 111)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 111 && resultCode == RESULT_OK) {
            filepath = data?.data!!

            try {

                val inputStream: InputStream = contentResolver.openInputStream(filepath)!!
                bitmap = BitmapFactory.decodeStream(inputStream)
                binding.imageViewProfile.setImageBitmap(bitmap)

            } catch (e: Exception) {
                Toast.makeText(this, "Something went wrong while loading image", Toast.LENGTH_SHORT).show()
            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun buttonUploadClicked() {
        uploadToFirebase()
    }

    private fun uploadToFirebase() {

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading")
        progressDialog.show()

        val firebaseStorage = FirebaseStorage.getInstance()
        val uploader = firebaseStorage.reference.child("image1.jpg")

        uploader.putFile(filepath)
                .addOnSuccessListener {
                    Toast.makeText(this, "File uploaded", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
                .addOnProgressListener {
                    val percent = (100 * it.bytesTransferred) / it.totalByteCount
                    progressDialog.setMessage("Uploaded : $percent%")
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Could not uplaod file", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
    }

}