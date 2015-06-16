import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.Document;
import java.io.*;
import java.util.ArrayList;

/**
 * obj: Clase para cambiar versión de un pdf y para hacer merge de varios pdf en una ruta.
 * author:jbenavides
 * */
public class SlkTools {

    public String fileNameIn;
    public String fileNameOut;
    public Character version = '4';
    public boolean deleteFile = true;
    public String pathToMerge;
    protected ArrayList<String> errors = new ArrayList<String>();

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

    public void setVersion(Character version) {
        this.version = version;
    }


    /*
    * obj: Lee un archivo pdf y la convierte a una versión menor o viceversa
    * author:jbenavides
    */
    public Boolean changeVersion(){
        Boolean resultado = false;
        if(this.getFileNameIn()==null || this.getFileNameOut()==null){
            this.errors.add("Faltan parametros.");
            return false;
        }
        try {
            String baseName = this.fileNameIn.substring(0,this.fileNameIn.lastIndexOf('/')+1);
            PdfReader reader = new PdfReader(this.getFileNameIn());
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(baseName.concat(this.getFileNameOut())),this.getVersion());
            stamper.close();
            reader.close();

            if(this.isDeleteFile()){
                this.deleteSafeFile(new File(this.getFileNameIn()));
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
        if(this.getPathToMerge()==null || this.getFileNameOut()==null){
            this.errors.add("Faltan parametros.");
            return false;
        }
        File folder = new File(this.getPathToMerge());
        File[] files = folder.listFiles();

        try {
            String fileNameOut = folder.getAbsolutePath()+"/"+this.getFileNameOut();
            String fileNameTmp = System.getProperty("java.io.tmpdir")+this.getFileNameOut();
            Document doc = new Document();
            FileOutputStream fileTmp = new FileOutputStream(fileNameTmp);
            PdfCopy copy = new PdfCopy(doc,fileTmp);
            int countPdfFiles=0;
            for(File file : files){

                doc.open();
                if(file.isFile() && this.getFileExtension(file).equals(".pdf")){
                    PdfReader reader = new PdfReader(file.getAbsolutePath());
                    for(int i=1; i<= reader.getNumberOfPages(); i++){
                        copy.addPage(copy.getImportedPage(reader,i));
                    }
                    copy.freeReader(reader);
                    reader.close();
                    countPdfFiles++;
                }

            }
            if(countPdfFiles>0){
                doc.close();
                this.copyFileToPath(new File(fileNameTmp),new File(fileNameOut));
                resultado=true;
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


//    public static void main(String[] args) {
//        SlkTools tool = new SlkTools();
////        tool.setPathToMerge("/Users/jbenavides/sites/ASE_2015/auditoria/actasFinales/1/OP");
////        tool.setFileNameOut("ActaFinal.pdf");
////        System.out.println(tool.doMerge());
////        System.out.println(tool.getErrors());
//        tool.setFileNameIn("/Users/jbenavides/Desktop/demoTool.pdf");
////        tool.setVersion('4');
//        tool.setDeleteFile(false);
////        tool.setFileNameOut("demo version.pdf");
//        System.out.println(tool.changeVersion());
//        System.out.println(tool.getErrors());
//    }


}
