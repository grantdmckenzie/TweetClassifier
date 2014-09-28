package com.geogrant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cc.mallet.classify.Classifier;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.types.Labeling;

/**
 * Servlet implementation class InferClass
 */
@WebServlet("/InferClass")
public class InferClass extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InferClass() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		if (request.getParameterMap().containsKey("t")) {
			String tweet = request.getParameter("t");
	        
		    if (tweet.length() >= 50) {
				// TODO Auto-generated method stub
				
				try {
					tweet = "1 personal "+tweet;
					Classifier classifier = loadClassifier();
					// File testset = new File(getServletContext().getRealPath("/WEB-INF/testing.csv"));
					printLabelings(classifier, tweet, out);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    } else {
				out.print("Please enter at least 50 characters for the tweet content.");
			}
		} else {
			out.print("Please enter content for the \"t\" parameters.");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		if (request.getParameterMap().containsKey("t")) {
			String tweet = request.getParameter("t");
	        
		    if (tweet.length() >= 50) {
				// TODO Auto-generated method stub
				
				try {
					tweet = "1 personal "+tweet;
					Classifier classifier = loadClassifier();
					// File testset = new File(getServletContext().getRealPath("/WEB-INF/testing.csv"));
					printLabelings(classifier, tweet, out);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    } else {
				out.print("Please enter at least 50 characters for the tweet content.");
			}
		} else {
			out.print("Please enter content for the \"t\" parameters.");
		}
	}
	
	public Classifier loadClassifier() throws FileNotFoundException, IOException, ClassNotFoundException {

        // The standard way to save classifiers and Mallet data                                            
        //  for repeated use is through Java serialization.                                                
        // Here we load a serialized classifier from a file.                                               

        Classifier classifier;

        File file = new File(getServletContext().getRealPath("/WEB-INF/tweets.classifier"));
        ObjectInputStream ois = new ObjectInputStream (new FileInputStream (file));
        classifier = (Classifier) ois.readObject();
        ois.close();

        return classifier;
    }
	
	public void printLabelings(Classifier classifier, String tweet, PrintWriter out) throws IOException {

		
		
        // Create a new iterator that will read raw instance data from                                     
        //  the lines of a file.                                                                           
        // Lines should be formatted as:                                                                   
        //                                                                                                 
        //   [name] [label] [data ... ]                                                                    
        //                                                                                                 
        //  in this case, "label" is ignored.                                                              

        CsvIterator reader = new CsvIterator(new StringReader(tweet), "(\\w+)\\s+(\\w+)\\s+(.*)", 3, 2, 1);  // (data, label, name) field indices               

        // Create an iterator that will pass each instance through                                         
        //  the same pipe that was used to create the training data                                        
        //  for the classifier.                                                                            
        Iterator instances = classifier.getInstancePipe().newIteratorFrom(reader);

        // Classifier.classify() returns a Classification object                                           
        //  that includes the instance, the classifier, and the                                            
        //  classification results (the labeling). Here we only                                            
        //  care about the Labeling.    
        out.print("{\"response\":[");
        while (instances.hasNext()) {
            Labeling labeling = classifier.classify(instances.next()).getLabeling();
            for (int rank = 0; rank < labeling.numLocations(); rank++){
                out.print("\""+labeling.getLabelAtRank(rank) + "\":" + labeling.getValueAtRank(rank) + "");
                if (rank < labeling.numLocations()-1)
                	out.print(", ");
            }
            // System.out.println();
        }
        out.print("]}");
    }

}
