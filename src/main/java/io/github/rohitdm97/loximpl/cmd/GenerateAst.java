package io.github.rohitdm97.loximpl.cmd;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate-ast <output directory>");
            System.exit(64);
        }

        String outputDir = args[0];
        String packageName = removeLast(GenerateAst.class.getPackageName()) + ".core";

        defineAst(outputDir, packageName, "Expr", Arrays.asList(
                "Assign   : Token name, Expr value",
                "Binary   : Expr left, Token operator, Expr right",
                "Call     : Expr callee, Token paren, List<Expr> arguments",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Logical  : Expr left, Token operator, Expr right",
                "Unary    : Token operator, Expr right",
                "Variable : Token name"
        ));

        defineAst(outputDir, packageName, "Stmt", Arrays.asList(
                "Block      : List<Stmt> statements",
                "Expression : Expr expression",
                "Function   : Token name, List<Token> params, List<Stmt> body",
                "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
                "Print      : Expr expression",
                "Return     : Token keyword, Expr value",
                "Var        : Token name, Expr initializer",
                "While      : Expr condition, Stmt body"
        ));
    }

    private static void defineAst(
            String outputDir,
            String packageName,
            String baseName,
            List<String> types
    ) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        IndentWriter writer = new IndentWriter(new PrintWriter(path, "UTF-8"));

        writer.println("package " + packageName + ";");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");
        writer.indent();

        defineVisitor(writer, baseName, types);

        // the AST classes
        for (String type : types) {
            final String[] splits = type.split(":");
            String className = splits[0].trim();
            String fields = splits[1].trim();
            defineType(writer, baseName, className, fields);
        }

        // the base accept() method
        writer.println();
        writer.println("abstract <R> R accept(Visitor<R> visitor);");

        if (writer.level != 1) {
            System.err.println("Invalid indentation detected does not return to normal indent");
        }
        writer.unindent("}");
        writer.close();
    }

    private static void defineVisitor(
            IndentWriter writer,
            String baseName, List<String> types
    ) {
        writer.println("interface Visitor<R> {");
        writer.indent();

        for (String type: types) {
            String typeName = type.split(":")[0].trim();
            String methodName = "visit" + typeName + baseName;
            writer.println(" R " + methodName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.unindent("}");
    }

    private static void defineType(
            IndentWriter writer,
            String baseName, String className, String fieldList
    ) {
        writer.println("static class " + className + " extends " + baseName + " {");
        writer.indent();

        // Constructor.
        writer.println(className + "(" + fieldList + ") {");
        writer.indent();

        // Store parameters in fields.
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("this." + name + " = " + name + ";");
        }

        writer.unindent("}");

        writer.println();
        writer.println("@Override");
        writer.println("<R> R accept(Visitor<R> visitor) {"); {
            writer.indent();
            writer.println("return visitor.visit" + className + baseName + "(this);");
            writer.unindent("}");
        }

        // Fields.
        writer.println();
        for (String field : fields) {
            writer.println("final " + field + ";");
        }

        writer.unindent("}");
    }

    private static String removeLast(String str) {
        final int idx = str.lastIndexOf('.');
        if (idx == -1) return str;
        return str.substring(0, idx);
    }

    static class IndentWriter implements AutoCloseable {
        final PrintWriter writer;
        int level = 0;
        static final String tab = "    ";

        public IndentWriter(PrintWriter writer) {
            this.writer = writer;
        }

        public void println(String line) {
            StringBuilder sb = new StringBuilder();
            sb.append(tab.repeat(Math.max(0, level)));
            sb.append(line);
            writer.println(sb);
        }

        public void println() {
            writer.println();
        }

        public void indent() {
            level++;
        }

        public void unindent(String closingLine) {
            level = Math.max(0, level - 1);
            println(closingLine);
        }

        public void close() {
            writer.close();
        }

    }

}
