package app.gerida;

public class Config {
	public static final String SERVER_URL = "http://192.168.1.36";
	// File upload url (replace the ip with your server address)
	public static final String FILE_UPLOAD_URL = SERVER_URL+"/dev/camera/UploadPicture.php";
	
	// Directory name to store captured images and videos
    public static final String IMAGE_DIRECTORY_NAME = "Gerida";

    public static int PAGE_ERROR = 0;

}