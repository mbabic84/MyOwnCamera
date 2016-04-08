package cz.kralicinora.myowncamera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import uk.co.senab.photoview.PhotoViewAttacher;

public class Main extends Activity {
    private static final int CAMERA_CAPTURE = 90, SAVE_IMAGE = 80;
    PhotoViewAttacher imageViewAttacher;
    private ImageView imageView;
    private ImageButton saveButton;
    private Uri tmpPhotoUri;
    private Uri newPhotoUri;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main );

        imageView = ( ImageView ) findViewById( R.id.main_imagepreview );
        imageViewAttacher = new PhotoViewAttacher( imageView );
        ImageButton imageButton = ( ImageButton ) findViewById( R.id.main_camerabutton );
        imageButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                try {
                    startExternalCameraActivityForResult();
                }
                catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        } );
        saveButton = ( ImageButton ) findViewById( R.id.main_savebutton );
        saveButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                Intent intent = new Intent( Intent.ACTION_CREATE_DOCUMENT );
                intent.setType( "image/jpeg" );
                startActivityForResult( intent, SAVE_IMAGE );
            }
        } );

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void startExternalCameraActivityForResult() throws IOException {
        Intent cameraIntent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
        if ( cameraIntent.resolveActivity( getPackageManager() ) == null ) {
            log( "No external camera app!" );
            return;
        }
        String photoName = String.valueOf( new Date().getTime() );
        File photo = File.createTempFile( photoName, ".jpg", getExternalCacheDir() );
        tmpPhotoUri = Uri.fromFile( photo );
        if ( photo.exists() ) {
            cameraIntent.putExtra( MediaStore.EXTRA_OUTPUT, tmpPhotoUri );
            startActivityForResult( cameraIntent, CAMERA_CAPTURE );
        }

    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        if ( resultCode == RESULT_OK && requestCode == CAMERA_CAPTURE ) {
            updateUi();
            log( "Photo captured!" );
        }
        else if ( resultCode == RESULT_OK && requestCode == SAVE_IMAGE ) {
            try {
                newPhotoUri = data.getData();
                copyFileUsingStream( tmpPhotoUri, newPhotoUri );
                tmpPhotoUri = null;
                updateUi();
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }

    private void log( String s ) {
        System.out.println( "#LOG#> " + s );
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUi();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState( Bundle outState ) {
        super.onSaveInstanceState( outState );
    }

    @Override
    protected void onRestoreInstanceState( Bundle savedInstanceState ) {
        super.onRestoreInstanceState( savedInstanceState );
    }

    private void updateUi() {
        Drawable drawable = null;
        if ( tmpPhotoUri != null && newPhotoUri == null ) {
            drawable = Drawable.createFromPath( tmpPhotoUri.getPath() );
        }
        else if ( newPhotoUri != null ) {
            try {
                ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor( newPhotoUri, "r" );
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                drawable = new BitmapDrawable( getResources(), BitmapFactory.decodeFileDescriptor( fileDescriptor ) );
            }
            catch ( FileNotFoundException e ) {
                e.printStackTrace();
            }
        }
        if ( tmpPhotoUri == null && newPhotoUri == null ) {
            saveButton.setVisibility( View.GONE );
        }
        else if ( newPhotoUri != null ) {
            saveButton.setVisibility( View.VISIBLE );
        }
        if ( drawable != null ) {
            imageView.setImageDrawable( drawable );
            imageViewAttacher.update();
        }
    }

    private void copyFileUsingStream( Uri source, Uri dest ) throws IOException {
        ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor( dest, "w" );
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream( new File( source.getPath() ) );
            os = new FileOutputStream( fileDescriptor.getFileDescriptor() );
            byte[] buffer = new byte[ 1024 ];
            int length;
            while ( ( length = is.read( buffer ) ) > 0 ) {
                os.write( buffer, 0, length );
            }
        }
        finally {
            assert is != null;
            is.close();
            assert os != null;
            os.close();
        }
    }
}
