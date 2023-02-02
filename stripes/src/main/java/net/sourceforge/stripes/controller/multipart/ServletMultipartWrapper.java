package net.sourceforge.stripes.controller.multipart;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.controller.FileUploadLimitExceededException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

public class ServletMultipartWrapper implements MultipartWrapper {

    private HttpServletRequest request;
    private File tempDir;

    @Override
    public void build(HttpServletRequest request, File tempDir, long maxPostSize) throws IOException, FileUploadLimitExceededException {
        this.request = request;
        this.tempDir = tempDir;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return request.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        return request.getParameterValues(name);
    }

    @Override
    public Enumeration<String> getFileParameterNames() {
        final Set<String> parameterNames = request.getParameterMap().keySet();
        try {
            return Collections.enumeration(request.getParts().stream().map(Part::getName).filter(name -> !parameterNames.contains(name)).toList());
        } catch (IOException | ServletException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FileBean getFileParameterValue(String name) {
        try {
            final Part part = request.getPart(name);
            if (part == null) {
                return null;
            }
            Path tempFile = Files.createTempFile(tempDir.toPath(), part.getSubmittedFileName(), null);
            part.write(tempFile.toAbsolutePath().toString());
            return new FileBean(tempFile.toFile(), part.getContentType(), part.getSubmittedFileName());
        } catch (IOException | ServletException e) {
            throw new RuntimeException(e);
        }
    }
}
