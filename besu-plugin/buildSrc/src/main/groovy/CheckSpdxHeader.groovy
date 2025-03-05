import groovy.io.FileType
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.BuildException

class CheckSpdxHeader extends DefaultTask {
    private String rootPath
    private String spdxHeader
    private String filesRegex
    private String excludeRegex

    @Input
    String getRootPath() {
        return rootPath
    }

    void setRootPath(final String rootPath) {
        this.rootPath = rootPath
    }

    @Input
    String getSpdxHeader() {
        return spdxHeader
    }

    void setSpdxHeader(final String spdxHeader) {
        this.spdxHeader = spdxHeader
    }

    @Input
    String getFilesRegex() {
        return filesRegex
    }

    void setFilesRegex(final String filesRegex) {
        this.filesRegex = filesRegex
    }

    @Input
    String getExcludeRegex() {
        return excludeRegex
    }

    void setExcludeRegex(final String excludeRegex) {
        this.excludeRegex = excludeRegex
    }

    @TaskAction
    void checkHeaders() {
        def filesWithoutHeader = []

        new File(rootPath).traverse(
                type: FileType.FILES,
                nameFilter: ~/${filesRegex}/,
                excludeFilter: ~/${excludeRegex}/
        ) {
            f ->
                if (!f.getText().contains(spdxHeader)) {
                    filesWithoutHeader.add(f)
                }
        }

        if (!filesWithoutHeader.isEmpty()) {
            throw new BuildException("Files without headers: " + filesWithoutHeader.join('\n'), null)
        }
    }
}