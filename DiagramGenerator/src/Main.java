import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;

import net.sf.sdedit.config.Configuration;
import net.sf.sdedit.config.ConfigurationManager;
import net.sf.sdedit.diagram.Diagram;
import net.sf.sdedit.editor.DiagramFileHandler;
import net.sf.sdedit.error.SemanticError;
import net.sf.sdedit.error.SyntaxError;
import net.sf.sdedit.server.Exporter;
import net.sf.sdedit.text.TextHandler;
import net.sf.sdedit.ui.ImagePaintDevice;
import net.sf.sdedit.ui.components.configuration.Bean;
import net.sf.sdedit.util.DocUtil.XMLException;
import net.sf.sdedit.util.Pair;


public class Main {

    public static void main(String[] args) {

//        String test = "bfs:BFS[a]\n/queue:FIFO\nsomeNode:Node\nnode:Node\nadjList:List\nadj:Node\n\nbfs:queue.new\n" + 
//                "bfs:someNode.setLevel(0)\nbfs:queue.insert(someNode)\n[c:loop while queue != ()]\nbfs:node=queue.remove()\n" +
//                "bfs:level=node.getLevel()\nbfs:adjList=node.getAdjacentNodes()\n[c:loop 0 <= i < #adjList]\n" +
//                "bfs:adj=adjList.get(i)\nbfs:nodeLevel=adj.getLevel()\n[c:alt nodeLevel IS NOT defined]\n" +
//                "bfs:adj.setLevel(level+1)\nbfs:queue.insert(adj)\n--[else]\n bfs:nothing to do\n[/c]\n [/c]\n [/c]\n" +
//                "bfs:queue.destroy()";
        
        SequenceDiagram testDiag = new SequenceDiagram();
        
        testDiag.AddObject(new SDObject("bfs", "BFS", Arrays.asList(ObjectFlag.ANONYMOUS)));
        testDiag.AddObject(new SDObject("/queue", "FIFO"));
        testDiag.AddObject(new SDObject("someNode", "Node"));
        testDiag.AddObject(new SDObject("node", "Node"));
        testDiag.AddObject(new SDObject("adjList", "List"));
        testDiag.AddObject(new SDObject("adjCool", "Node"));

        testDiag.AddMessage(new SDMessage("bfs", "new", opts("queue","","","")));
        testDiag.AddMessage(new SDMessage("bfs", "setLevel(0)", opts("someNode","", "", "")));
        testDiag.AddMessage(new SDMessage("bfs", "insert(someNode)", opts("queue","","","")));

        System.out.println(testDiag.toString());
        testDiag.CreatePDF("testDiagram.pdf");
    }
    
    private static Dictionary<MessageOpt, String> opts(String callee, String answer, String specifier, String mnemonic) {
        
        Dictionary<MessageOpt, String> msg = new Hashtable<MessageOpt, String>();
        if (!callee.isEmpty()) msg.put(MessageOpt.CALLEE, callee);
        if (!answer.isEmpty()) msg.put(MessageOpt.ANSWER, answer);
        if (!specifier.isEmpty()) msg.put(MessageOpt.SPECIFIER, specifier);
        if (!mnemonic.isEmpty()) msg.put(MessageOpt.MNEMONIC, mnemonic);
        return msg;
        
    }

}
