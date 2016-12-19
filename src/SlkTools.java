import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * obj: Clase para cambiar versión de un pdf y para hacer merge de varios pdf en una ruta.
 * author:jbenavides
 * */
public class SlkTools {

    public String fileNameIn;
    public String fileNameOut;
    public String pathFileNameOut;

    public void setPathFileNameOut(String pathFileNameOut) {
        this.pathFileNameOut = pathFileNameOut;
    }

    public void setPathFileNameIn(String pathFileNameIn) {
        this.pathFileNameIn = pathFileNameIn;
    }

    public String pathFileNameIn;
    public Character version = '4';
    public boolean deleteFile = true;
    public String pathToMerge;
    public ArrayList<String> paths = new ArrayList<String>();
    public ArrayList<String> validExtensions = new ArrayList<String>(Arrays.asList("jpg","jpeg","png","gif","pdf"));
    public boolean enableImages = false;
    public File[] filesPath = new File[0];
    protected ArrayList<String> errors = new ArrayList<String>();

    public boolean isEnableImages() {
        return enableImages;
    }

    public void setEnableImages(boolean enableImages) {
        this.enableImages = enableImages;
    }

    public void setValidExtensions(ArrayList<String> validExtensions) {
        this.validExtensions = validExtensions;
    }

    public ArrayList<String> getValidExtensions() {

        return validExtensions;
    }

    public File[] getFilesPath() {
        return filesPath;
    }

    public void setFilesPath(ArrayList<String> paths) {
        ArrayList<File> arrayFiles = new ArrayList<File>();

        for(String s : paths){
            arrayFiles.add(new File(s));
        }
        this.filesPath = arrayFiles.toArray(new File[0]);
    }

    public ArrayList<String> getPaths() {
        return paths;
    }

    public void setPaths(ArrayList<String> paths) {
        this.paths = paths;
    }

    public String getPathToMerge() {
        return pathToMerge;
    }

    public void setPathToMerge(String pathToMerge) {
        this.pathToMerge = pathToMerge;
    }

    public ArrayList<String> getErrors() {
        return errors;
    }

    public boolean isDeleteFile() {
        return deleteFile;
    }

    public void setDeleteFile(boolean deleteFile) {
        this.deleteFile = deleteFile;
    }

    public String getFileNameIn() {
        return fileNameIn;
    }

    public void setFileNameIn(String fileNameIn) {
        this.fileNameIn = fileNameIn;
    }

    public String getFileNameOut() {
        return fileNameOut;
    }

    public void setFileNameOut(String fileNameOut) {
        this.fileNameOut = fileNameOut;
    }

    public Character getVersion() {
        return version;
    }

    public String getPathFileNameOut() {
        return pathFileNameOut;
    }

    public String getPathFileNameIn() {
        return pathFileNameIn;
    }

    public void setVersion(Character version) {
        this.version = version;
    }


    /*
    * obj: Lee un archivo pdf y la convierte a una versión menor o viceversa
    * author:jbenavides
    *
    * actualización 13 de diciembre 2016 (actualizacion a ruta completa para
    * guardar el archivo en una ruta diferente)
    */
    public Boolean changeVersion(){
        Boolean resultado = false;
        if(this.getPathFileNameIn()==null || this.getPathFileNameOut()==null){
            this.errors.add("Faltan parametros.");
            return false;
        }
        try {
            String baseNameOut = this.pathFileNameOut;
            PdfReader reader = new PdfReader(this.getPathFileNameIn());
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(baseNameOut),this.getVersion());
            stamper.close();
            reader.close();

            if(this.isDeleteFile()){
                this.deleteSafeFile(new File(this.getPathFileNameIn()));
            }
            resultado =true;

        } catch (IOException e) {
            this.errors.add(e.getMessage());
            //e.printStackTrace();
        } catch (DocumentException e) {
            this.errors.add(e.getMessage());
            //e.printStackTrace();
        }
        return resultado;

    }
    /**
    * obj: Lee un directorio y busca los archivos pdf y los conjunta en uno solo.
    * author:jbenavides
    */
    public boolean doMerge(){

        Boolean resultado = false;
        File[] files = this.getFiles();
        if(this.getFileNameOut()==null){
            this.errors.add("Faltan parametros.");
            return false;
        }

        try {
            String fileNameOut = this.getFileName(this.getFileNameOut());
            String fileNameTmp = System.getProperty("java.io.tmpdir")+fileNameOut;
            Document doc = new Document();
            FileOutputStream fileTmp = new FileOutputStream(fileNameTmp);
            PdfCopy copy = new PdfCopy(doc,fileTmp);
            int countPdfFiles=0;
            for(File file : files){
                boolean isImage = false;
                doc.open();
                String ext = this.getFileExtension(file).substring(1);
                if(file.isFile() && this.getValidExtensions().contains(ext)){
                    if(ext.equals("pdf") || (this.enableImages && this.getValidExtensions().contains(ext))){
                        if(!ext.equals("pdf")){
                            file = this.imageToPDF(file,System.getProperty("java.io.tmpdir"));
                            isImage=true;
                        }
                        PdfReader reader = new PdfReader(file.getAbsolutePath());
                        for(int i=1; i<= reader.getNumberOfPages(); i++){
                            copy.addPage(copy.getImportedPage(reader,i));
                        }
                        copy.freeReader(reader);
                        reader.close();
                        countPdfFiles++;
                        if(isImage){
                            this.deleteSafeFile(file);
                        }
                    }

                }

            }
            if(countPdfFiles>0){
                doc.close();
                this.copyFileToPath(new File(fileNameTmp),new File(this.getFileNameOut()));
                this.deleteSafeFile(new File(fileNameTmp));
                resultado=true;
            }else{
                this.errors.add("No hay archivos Pdf en el directorio");
            }

        } catch (FileNotFoundException e) {
            this.errors.add(e.getMessage());
            //e.printStackTrace();
        } catch (DocumentException e) {
            this.errors.add(e.getMessage());
            //e.printStackTrace();
        } catch (IOException e) {
            this.errors.add(e.getMessage());
            //e.printStackTrace();
        }

        return resultado;
    }

    public void log(String out){
        System.out.println(out);
    }

    /**
    * obj: Elimina un archivo si existe.
    * author:jbenavides
    */
    private void deleteSafeFile(File file){
        if(file.exists()){
            file.delete();
        }

    }

    /**
    * obj: Obtener la extensión del archivo
    * author:jbenavides
    */
    private String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf("."));

        } catch (Exception e) {
            return "";
        }

    }
    /**
     * obj: Copiar un archivo.
     * author:jbenavides
     */
    private void copyFileToPath(File fileOrigen,File fileDestino){
        byte[] readData = new byte[1024];
        try {
            FileInputStream fis = new FileInputStream(fileOrigen);
            FileOutputStream fos = new FileOutputStream(fileDestino);
            int i = fis.read(readData);
            while (i != -1){
                fos.write(readData,0,i);
                i = fis.read(readData);
            }
            fis.close();
            fos.close();
        } catch (FileNotFoundException e) {
            this.errors.add(e.getMessage());
            //e.printStackTrace();
        } catch (IOException e) {
            this.errors.add(e.getMessage());
            //e.printStackTrace();
        }


    }
    /**
     * obj: Definir la lista de archivos para hacer el merge. dependiendo si es un directorio o un array de paths.
     * author:jbenavides
     */
    public File[] getFiles(){
        File[] files = new File[0];
        if(this.getFilesPath().length>0){
            files =this.getFilesPath();
        }else if(this.getPathToMerge()!=null){
            files = new File(this.getPathToMerge()).listFiles();
        }
        return files;
    }

    /**
     * obj: Obtener el nombre del archivo sin el path.
     * author:jbenavides
     */
    public String getFileName(String path){
        return path.substring(path.lastIndexOf('/')+1);

    }

    public File imageToPDF(File file, String filePathTmp){

        String ext = this.getFileExtension(file).substring(1);
        String nameFile = file.getName().substring(0, file.getName().lastIndexOf('.'));
        String fileNameTmp =filePathTmp+nameFile+".pdf";

        if(this.getValidExtensions().contains(ext)) {
            Document document = new Document();
            try {
                PdfWriter.getInstance(document,
                        new FileOutputStream(fileNameTmp));
                document.open();
                Image image = Image.getInstance(file.getAbsolutePath());
                document.add(image);
                document.close();
            } catch(Exception e){
                e.printStackTrace();
            }

        }
        return new File(fileNameTmp);
    }


    public static void main(String[] args) {
        SlkTools tool = new SlkTools();
            //tool.setPathToMerge("/Users/jbenavides/Desktop/MergePdf");
            //tool.setEnableImages(true);
            ArrayList<String> files = new ArrayList<String>(Arrays.asList("/Users/jbenavides/Sites/Nomina2/dev/Expedientes/SALS560726HZSNGL04/38532.pdf","/Users/jbenavides/Sites/Nomina2/dev/Expedientes/SALS560726HZSNGL04/38533.jpg"));
            tool.setFilesPath(files);
            tool.setEnableImages(true);
            tool.setFileNameOut("/Users/jbenavides/Desktop/MergePdf/MergeFiles.pdf");
            tool.doMerge();

//        tool.setPathToMerge("/Users/jbenavides/Desktop/actasFinales/3/3/");
//        tool.setFileNameOut("merge999.pdf");
//        tool.doMerge();

//        tool.setPathToMerge("/Users/jbenavides/sites/ASE_2015/auditoria/actasFinales/1/");
//        tool.setFileNameOut("/Users/jbenavides/sites/ASE_2015/auditoria/actasFinales/1/OP/ActaFinal-itera2.pdf");
//        System.out.println(tool.doMerge());
//        System.out.println(tool.getErrors());
//        tool.setFileNameIn("/Users/jbenavides/Desktop/demoTool.pdf");
//        tool.setVersion('4');
//        tool.setDeleteFile(false);
//        tool.setFileNameOut("demo version.pdf");
//        System.out.println(tool.changeVersion());
//        ArrayList<String> paths = new ArrayList<String>();
//        paths.add("/Users/jbenavides/sites/ASE_2015/auditoria/actasFinales/1/OP/PR-PF-FI-01-F47 15.08.13.pdf");
//        paths.add("/Users/jbenavides/sites/ASE_2015/auditoria/actasFinales/1/OP/_Scanned-image.pdf");
//
//        tool.setFilesPath(paths);
//        System.out.println(tool.doMerge());
////        System.out.println(tool.doMergePaths());
//        System.out.println(tool.getErrors());
    }


}
