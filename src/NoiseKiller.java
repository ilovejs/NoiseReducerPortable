import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.phonon.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

public class NoiseKiller extends QMainWindow{

    public NoiseKiller() throws IOException
    {
        audioOutput = new AudioOutput(Phonon.Category.MusicCategory);
        mediaObject = new MediaObject(this);
        metaInformationResolver = new MediaObject(this);

        Phonon.createPath(mediaObject, audioOutput);

        mediaObject.setTickInterval(1000);

        mediaObject.tick.connect(this, "tick(long)");
        mediaObject.stateChanged.connect(this, "stateChanged(Phonon$State,Phonon$State)");
        metaInformationResolver.stateChanged.
                connect(this, "metaStateChanged(Phonon$State,Phonon$State)");
        mediaObject.currentSourceChanged.connect(this, "sourceChanged(MediaSource)");
        mediaObject.aboutToFinish.connect(this, "aboutToFinish()");

        setupActions();
        setupMenus();
        setupUi();
        timeLcd.display("00:00"); 
    }

    private void addFiles()
    {
    	List<String> files = QFileDialog.getOpenFileNames(this,
                            tr("Select Music Files"), ".");  
        if (files.isEmpty())
            return;

        int index = sources.size();
        for (String string : files) {
            MediaSource source = new MediaSource(string);
        
            sources.add(source);
        } 
        if (!sources.isEmpty())
            metaInformationResolver.setCurrentSource(sources.get(index));
    }

    private void about()
    {
        QMessageBox.information(this, tr("About Music Player"),
            tr("Noise Reducer can overwrite many noises in your surroundings." +
               " It help maintain focus on work :D\n 消除讨厌的环境噪音从我做起！不论是闹钟，还是讲话声，全波段消除有木有，复习考试利器，寝室瞬间清净了\n" + "Autho: Tianhao Zhuang\n" + "微博: weibo.com/dailyjava" ));
    }

    private void stateChanged(Phonon.State newState, Phonon.State oldState)
    {
        switch (newState) {
            case ErrorState:
                if (mediaObject.errorType().equals(Phonon.ErrorType.FatalError)) {
                    QMessageBox.warning(this, tr("Fatal Error"),
                    mediaObject.errorString());
                } else {
                    QMessageBox.warning(this, tr("Error"),
                    mediaObject.errorString());
                }
                break;
            case PlayingState:
                playAction.setEnabled(false);
                pauseAction.setEnabled(true);
                stopAction.setEnabled(true);
                break;
            case StoppedState:
                stopAction.setEnabled(false);
                playAction.setEnabled(true);
                pauseAction.setEnabled(false);
                timeLcd.display("00:00");
                break;
            case PausedState:
                pauseAction.setEnabled(false);
                stopAction.setEnabled(true);
                playAction.setEnabled(true);
                break;
            case BufferingState:
                break;
        }
    }

    private void tick(long time)
    {
        QTime displayTime = new QTime(0, (int) (time / 60000) % 60, (int) (time / 1000) % 60);

        timeLcd.display(displayTime.toString("mm:ss"));
    }

    private void tableClicked(int row, int column)
    {
        boolean wasPlaying = mediaObject.state().equals(Phonon.State.PlayingState);

        mediaObject.stop();
        mediaObject.clearQueue();

        mediaObject.setCurrentSource(sources.get(row));

        if (wasPlaying) 
            mediaObject.play();
        else
            mediaObject.stop();
    }

    private void sourceChanged(MediaSource source)
    {
        musicTable.selectRow(sources.indexOf(source));

        timeLcd.display("00:00");
    }

    private void metaStateChanged(Phonon.State newState, Phonon.State oldState)
    {
        if (newState.equals(Phonon.State.ErrorState)) {
            QMessageBox.warning(this, tr("Error opening files"),
                metaInformationResolver.errorString());
            while (!sources.isEmpty() &&
                   !(sources.remove(sources.size() - 1).equals(metaInformationResolver.currentSource())));
            return;
        }

        if (!newState.equals(Phonon.State.StoppedState))
            return;

        if (metaInformationResolver.currentSource().type().equals(MediaSource.Type.Invalid))
                return;

        Map<String, List<String>> metaData = metaInformationResolver.metaData();

        String title = "";
        if (metaData.get("TITLE") != null)
            title = metaData.get("TITLE").get(0);

        if (title.equals(""))
            title = metaInformationResolver.currentSource().fileName();

        String artist = "";
        if (metaData.get("ARTIST") != null)
            artist = metaData.get("ARTIST").get(0);

        QTableWidgetItem titleItem = new QTableWidgetItem(title);
        QTableWidgetItem artistItem = new QTableWidgetItem(artist);

        int currentRow = musicTable.rowCount();
        musicTable.insertRow(currentRow);
        musicTable.setItem(currentRow, 0, titleItem);
        musicTable.setItem(currentRow, 1, artistItem);

        if (musicTable.selectedItems().isEmpty()) {
            musicTable.selectRow(0);
            mediaObject.setCurrentSource(metaInformationResolver.currentSource());
        }

        MediaSource source = metaInformationResolver.currentSource();
        int index = sources.indexOf(metaInformationResolver.currentSource()) + 1;
        if (sources.size() > index) {
            metaInformationResolver.setCurrentSource(sources.get(index));
        }
        else {
            musicTable.resizeColumnsToContents();
            if (musicTable.columnWidth(0) > 300)
                musicTable.setColumnWidth(0, 300);
        }
    }
    //select row and enqueue
    private void aboutToFinish(){
      //repeat same song until user click another one in Table
      int index = sources.indexOf(mediaObject.currentSource());
        if (sources.size() > index) {
            mediaObject.enqueue(sources.get(index));
            musicTable.selectRow(index);
        }
    }

    private void setupActions()
    {
        playAction = new QAction(new QIcon("classpath:res/play.png"), tr("Play"), this);
        playAction.setShortcut(tr("Crl+P"));
        playAction.setDisabled(true);
        pauseAction = new QAction(new QIcon("classpath:res/pause.png"), tr("Pause"), this);
        pauseAction.setShortcut(tr("Ctrl+A"));
        pauseAction.setDisabled(true);
        stopAction = new QAction(new QIcon("classpath:res/stop.png"), tr("Stop"), this);
        stopAction.setShortcut(tr("Ctrl+S"));
        stopAction.setDisabled(true);
        addFilesAction = new QAction(tr("Add &Files"), this);
        addFilesAction.setShortcut(tr("Ctrl+F"));
        exitAction = new QAction(tr("E&xit"), this);
        exitAction.setShortcut(tr("Ctrl+X"));
        aboutAction = new QAction(tr("A&bout"), this);
        aboutAction.setShortcut(tr("Ctrl+B"));

        playAction.triggered.connect(mediaObject, "play()");
        pauseAction.triggered.connect(mediaObject, "pause()");
        stopAction.triggered.connect(mediaObject, "stop()");
        addFilesAction.triggered.connect(this, "addFiles()");
        exitAction.triggered.connect(this, "close()");
        aboutAction.triggered.connect(this, "about()");
    }

    private void setupMenus()
    {
        QMenu fileMenu = menuBar().addMenu(tr("&File"));
        fileMenu.addAction(addFilesAction);
        fileMenu.addSeparator();
        fileMenu.addAction(exitAction);

        QMenu aboutMenu = menuBar().addMenu(tr("&Help"));
        aboutMenu.addAction(aboutAction);
    }

    protected void toggleVisibility() {
        if (isVisible())
            hide();
        else
            show();
    }
    
    protected void updateMenu() {
        toggleVisibilityAction.setText(isVisible() ? tr("Hide") : tr("Show"));
    }
    
    protected void balloonMsg() {
    	QSystemTrayIcon.MessageIcon icon;
        icon = QSystemTrayIcon.MessageIcon.resolve(0);
        trayIcon.showMessage("Guide", "Left click to Hide\nRight click to show menu",icon, 10000);
        trayIcon.setToolTip("Right Click to show menu");
    }
    
    private void setupUi() throws IOException{
    	trayIconMenu = new QMenu(this);
    	trayIconMenu.aboutToShow.connect(this, "updateMenu()");

        // Create the tray icon
        trayIcon = new QSystemTrayIcon(this);
        trayIcon.setToolTip("Right Click to see Menu~");
        trayIcon.setContextMenu(trayIconMenu);
        
//        trayIcon.messageClicked.connect(this, "toggleVisibility()");
        trayIcon.activated.connect(this, "toggleVisibility()");
        
        String iconName = "classpath:res/icon.png";
        QPixmap pixmap = new QPixmap(iconName);
        trayIcon.setIcon(new QIcon(pixmap));
        trayIcon.show();

        toggleVisibilityAction = new QAction("Show/Hide", this);
        toggleVisibilityAction.triggered.connect(this, "toggleVisibility()");
        trayIconMenu.addAction(toggleVisibilityAction);
        
        QAction restoreAction = new QAction("Restore", this);
        restoreAction.triggered.connect(this, "showNormal()");
        trayIconMenu.addAction(restoreAction);
        
        trayIconMenu.addSeparator();
        QAction quitAction = new QAction("&Quit", this);
        quitAction.triggered.connect(this, "close()");
        trayIconMenu.addAction(quitAction);

        QToolBar bar = new QToolBar();
        bar.addAction(playAction);
        bar.addAction(pauseAction);
        bar.addAction(stopAction);
    
        seekSlider = new SeekSlider(this);
        seekSlider.setMediaObject(mediaObject);

        volumeSlider = new VolumeSlider(this);
//        volumeSlider.setMaximumVolume(2.0);
        volumeSlider.setAudioOutput(audioOutput);
        volumeSlider.setSizePolicy(QSizePolicy.Policy.Maximum, QSizePolicy.Policy.Maximum);

        QLabel volumeLabel = new QLabel();
//        volumeLabel.setPixmap(new QPixmap("classpath:res/volume.png"));

        QPalette palette = new QPalette();
        palette.setBrush(QPalette.ColorRole.Light, new QBrush(QColor.darkGray));

        timeLcd = new QLCDNumber();
        timeLcd.setPalette(palette);

        List<String> headers = new Vector<String>();
        headers.add(tr("Source"));
        headers.add(tr("Method"));
        headers.add(tr("Album"));
        headers.add(tr("Year"));

        musicTable = new QTableWidget(0, 2);
        musicTable.setHorizontalHeaderLabels(headers);
        musicTable.setSelectionMode(QAbstractItemView.SelectionMode.SingleSelection);
        musicTable.setSelectionBehavior(QAbstractItemView.SelectionBehavior.SelectRows);
        musicTable.cellPressed.connect(this, "tableClicked(int,int)");
        //不可编辑
        musicTable.setEditTriggers(QAbstractItemView.EditTrigger.NoEditTriggers);
        
        ////////////////////////////
        
        
        
        tempdir = System.getProperty("user.dir");// java.io.tmpdir
        
//        extract();
        String writenoise = tempdir + "\\wn.mp3";
    	String rednoise = tempdir + "\\rn.mp3";
    	String brownnoise = tempdir + "\\bn.mp3";
    	
//        List<String> files = new ArrayList<String>();
//        files.add(writenoise);
//        files.add(rednoise);
//        files.add(brownnoise);

//        for (String string : files) {
//            MediaSource source = new MediaSource(string);
//            sources.add(source);
//        } 
    	
        sources.add(new MediaSource(writenoise));
        sources.add(new MediaSource(rednoise));
        sources.add(new MediaSource(brownnoise));
        if (!sources.isEmpty())
            metaInformationResolver.setCurrentSource(sources.get(0));
        ///////////////////////////
        
        QHBoxLayout seekerLayout = new QHBoxLayout();
        seekerLayout.addWidget(seekSlider);
        seekerLayout.addWidget(timeLcd);

        QHBoxLayout playbackLayout = new QHBoxLayout();
        playbackLayout.addWidget(bar);
        playbackLayout.addStretch();
        playbackLayout.addWidget(volumeLabel);
        playbackLayout.addWidget(volumeSlider);

        QVBoxLayout mainLayout = new QVBoxLayout();
        mainLayout.addWidget(musicTable);
        mainLayout.addLayout(seekerLayout);
        mainLayout.addLayout(playbackLayout);

        QWidget widget = new QWidget();
        widget.setLayout(mainLayout);

        setCentralWidget(widget);
        setWindowTitle("Environment Noise Reducer");
        setWindowIcon(new QIcon("classpath:res/icon.png"));
        
        balloonMsg();
    
    }

//    private void extract() throws IOException {
//    	File f1 = new File(tempdir + "wn.mp3");
//    	File f2 = new File(tempdir + "rn.mp3");
//    	File f3 = new File(tempdir + "bn.mp3");
//    	System.out.println(f1.toString());
//    	if(f1.exists() && f2.exists() && f3.exists()){
//    		return;
//    	}
//    	//read package file
//    	InputStream is = this.getClass().getClassLoader().getResourceAsStream("wn.mp3");
//    	FileOutputStream fos = new FileOutputStream(tempdir + "wn.mp3"); //定义一个  
//    	
//    	int read = 0;
//    	byte[] bytes = new byte[8024];
//    	while ((read = is.read(bytes)) != -1) {
//    		fos.write(bytes, 0, read);
//    	}
//    	is.close();
//    	fos.flush();
//    	fos.close();
//    	
//	}
    String tempdir;
	private QSystemTrayIcon trayIcon;
    private QMenu trayIconMenu;
    private QAction toggleVisibilityAction;
    private SeekSlider seekSlider;
    private MediaObject mediaObject;
    private MediaObject metaInformationResolver;
    private AudioOutput audioOutput;
    private VolumeSlider volumeSlider;
    private List<MediaSource> sources = new Vector<MediaSource>();

    private QAction playAction;
    private QAction pauseAction;
    private QAction stopAction;
    private QAction addFilesAction;
    private QAction exitAction;
    private QAction aboutAction;
    private QLCDNumber timeLcd;
    private QTableWidget musicTable;

    public static void main(String args[]) throws IOException{
            QApplication.initialize(args);
            QApplication.setApplicationName("Noise Reducer");
            NoiseKiller app = new NoiseKiller();
            app.resize(500, 270);
            app.move(500, 250);
            app.show();
            
            QApplication.exec();
    }
}