package dev.kmpilot.linux.data

import dev.kmpilot.linux.domain.Command

/**
 * The bundled, offline command reference — real Linux commands (this is why LinuxCommandLibrary is the port
 * we can make genuinely FUNCTION with no backend). The repository is the only data seam; search/filter are
 * first-class, not vibe-coded per screen.
 */
class CommandRepository(private val all: List<Command> = COMMANDS) {
    fun categories(): List<String> = all.map { it.category }.distinct().sortedBy { it }
    fun search(query: String): List<Command> {
        val q = query.trim().lowercase()
        return (if (q.isEmpty()) all else all.filter { it.name.contains(q) || it.summary.lowercase().contains(q) })
            .sortedBy { it.name }
    }
    fun byCategory(category: String): List<Command> = all.filter { it.category == category }.sortedBy { it.name }
    fun byName(name: String): Command? = all.find { it.name == name }
    fun count(category: String): Int = all.count { it.category == category }
}

private val COMMANDS = listOf(
    Command("ls", "Files", "List directory contents.", "ls [OPTION]... [FILE]...",
        listOf("ls -la", "ls -lh /var/log", "ls -t  # newest first")),
    Command("cat", "Files", "Concatenate files and print on the standard output.", "cat [OPTION]... [FILE]...",
        listOf("cat notes.txt", "cat a.txt b.txt > both.txt", "cat -n script.sh  # number lines")),
    Command("cp", "Files", "Copy files and directories.", "cp [OPTION]... SOURCE DEST",
        listOf("cp file.txt backup.txt", "cp -r src/ dst/", "cp -i a b  # prompt before overwrite")),
    Command("mv", "Files", "Move (rename) files.", "mv [OPTION]... SOURCE DEST",
        listOf("mv old.txt new.txt", "mv *.png images/", "mv -n a b  # never overwrite")),
    Command("rm", "Files", "Remove files or directories.", "rm [OPTION]... FILE...",
        listOf("rm tmp.txt", "rm -r build/", "rm -i *.log  # confirm each")),
    Command("mkdir", "Files", "Make directories.", "mkdir [OPTION]... DIRECTORY...",
        listOf("mkdir project", "mkdir -p a/b/c  # create parents")),
    Command("cd", "Navigation", "Change the shell working directory.", "cd [DIR]",
        listOf("cd /etc", "cd ..  # up one", "cd -  # previous directory")),
    Command("pwd", "Navigation", "Print the name of the current working directory.", "pwd [OPTION]",
        listOf("pwd", "pwd -P  # resolve symlinks")),
    Command("grep", "Search", "Print lines that match a pattern.", "grep [OPTION]... PATTERN [FILE]...",
        listOf("grep error log.txt", "grep -ri todo src/", "grep -n main *.kt  # show line numbers")),
    Command("find", "Search", "Search for files in a directory hierarchy.", "find [PATH]... [EXPRESSION]",
        listOf("find . -name '*.kt'", "find /tmp -type f -mtime +7", "find . -size +10M")),
    Command("chmod", "Permissions", "Change file mode bits.", "chmod [OPTION]... MODE FILE...",
        listOf("chmod +x run.sh", "chmod 644 file.txt", "chmod -R 755 public/")),
    Command("chown", "Permissions", "Change file owner and group.", "chown [OPTION]... OWNER[:GROUP] FILE...",
        listOf("chown user file.txt", "chown -R me:devs project/")),
    Command("ps", "Processes", "Report a snapshot of the current processes.", "ps [OPTION]...",
        listOf("ps aux", "ps -ef | grep java", "ps -p 1234")),
    Command("kill", "Processes", "Send a signal to a process.", "kill [-SIGNAL] PID...",
        listOf("kill 1234", "kill -9 1234  # force", "kill -HUP $(pgrep nginx)")),
    Command("top", "Processes", "Display Linux processes (live).", "top [OPTION]...",
        listOf("top", "top -o %MEM  # sort by memory")),
    Command("tar", "Archives", "Store and extract files from an archive.", "tar [OPTION]... [FILE]...",
        listOf("tar -czf out.tgz dir/", "tar -xzf out.tgz", "tar -tf out.tgz  # list contents")),
    Command("ssh", "Network", "OpenSSH remote login client.", "ssh [USER@]HOST [COMMAND]",
        listOf("ssh me@server", "ssh -p 2222 me@host", "ssh me@host 'uptime'")),
    Command("curl", "Network", "Transfer data from or to a server.", "curl [OPTIONS] URL",
        listOf("curl https://example.com", "curl -O https://host/file.zip", "curl -s api/health | jq")),
    Command("df", "System", "Report file system disk space usage.", "df [OPTION]... [FILE]...",
        listOf("df -h", "df -h /  # root filesystem")),
    Command("du", "System", "Estimate file space usage.", "du [OPTION]... [FILE]...",
        listOf("du -sh *", "du -h --max-depth=1", "du -sh node_modules")),
)
