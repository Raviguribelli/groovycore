package groovy.ui.view

import javax.swing.JComponent
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext
import org.codehaus.groovy.runtime.InvokerHelper

build(Defaults)

// change font to DejaVu Sans Mono, much clearer
styles.regular[StyleConstants.FontFamily] = 'DejaVu Sans Mono'
styles[StyleContext.DEFAULT_STYLE][StyleConstants.FontFamily] = 'DejaVu Sans Mono'

// possibly change look and feel
if (System.properties['java.version'] =~ /^1\.5/) {
    // GTK wasn't where it needed to be in 1.5, especially with toolbars
    // use metal instead
    lookAndFeel('metal', boldFonts:false)
    
    // we also need to turn on anti-alising ourselves
    key = InvokerHelper.getProperty('com.sun.java.swing.SwingUtilities2' as Class,
        'AA_TEXT_PROPERTY_KEY')
    addAttributeDelegate {builder, node, attributes ->
        if (node instanceof JComponent) {
            node.putClientProperty(key, new Boolean(true));
        }
    }
}

// some current distros (Ubuntu 7.10) have broken printing support :(
// detect it and disable it
try {
    pj = java.awt.print.PrinterJob.getPrinterJob()
    ps = pj.getPrintService()
    ps.getAttributes()
    docFlav  = (ps.getSupportedDocFlavors() as List).find {it.mimeType == 'application/vnd.cups-postscript' }
    attrset = ps.getAttributes()
    orient = attrset.get(javax.print.attribute.standard.OrientationRequested) ?: 
             ps.getDefaultAttributeValue(javax.print.attribute.standard.OrientationRequested)
    ps.isAttributeValueSupported(orient, docFlav, attrset)
} catch (NullPointerException npe) {
    //print will bomb out... replace with disabled print action
    action(id: 'printAction',
        name: 'Print...',
        closure: controller.&print,
        mnemonic: 'P',
        accelerator: shortcut('P'),
        shortDescription: 'Printing does not work in Java with this version of CUPS',
        enabled: false
    )
}



