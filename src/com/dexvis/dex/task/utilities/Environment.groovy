package com.dexvis.dex.task.utilities

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TableColumn.CellEditEvent
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.util.Callback

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.tbee.javafx.scene.layout.MigPane

import com.dexvis.dex.exception.DexException
import com.dexvis.dex.wf.DexEnvironment
import com.dexvis.dex.wf.DexTask
import com.dexvis.dex.wf.DexTaskState
import com.dexvis.javafx.event.ReflectiveActionEventHandler
import com.dexvis.javafx.observable.NameValuePair
import com.dexvis.javafx.scene.control.EnvironmentEditingCell
import com.dexvis.javafx.scene.control.ModalDialog

/**
 * 
 * This task allow the user to manage environment variables.
 * 
 * @author Patrick Martin
 *
 */
@Root
class Environment extends DexTask {
  // Used to store our configuration pane.
  private MigPane configPane = null
  @ElementList(name = "environmentData", inline = true, required = false)
  private ObservableList<NameValuePair> envData     = FXCollections
  .observableArrayList()
  TableView<NameValuePair> envTable = new TableView<NameValuePair>()
  DexEnvironment env = DexEnvironment.getInstance()

  /**
   * 
   * Override the default constructor to provide this component's name, category and help file.
   * Report that it has been constructed.
   * 
   */
  public Environment()
  {
    super("Utilities", "Environment", "utilities/Environment.html")


    // Set at initialization
    envData.each
    {
      println "ENVIRONMENT: ${it.name}=${it.value}"
      env.setVariable(it.name, it.value)
    }
  }

  public DexTaskState initialize(DexTaskState state) throws DexException
  {
    envData.each
    {
      println "ENVIRONMENT: ${it.name}=${it.value}"
      env.setVariable(it.name, it.value)
    }
  }

  public DexTaskState execute(DexTaskState state) throws DexException
  {
    try
    {
      envData.each
      {
        println "ENVIRONMENT: ${it.name}=${it.value}"
        env.setVariable(it.name, it.value)
      }
    }
    catch(Exception ex)
    {
      StringWriter sw = new StringWriter()
      ex.printStackTrace(new PrintWriter(sw))
      ModalDialog dialog = new ModalDialog(stage, "Error", sw.toString(), "Ok")
      ex.printStackTrace()
    }

    return state
  }

  public Node getConfig()
  {
    if (configPane == null)
    {
      configPane = new MigPane("", "[grow]", "[grow][]")
      configPane.setStyle("-fx-background-color: white;")

      TableColumn nameCol = new TableColumn("Name")
      nameCol.setCellValueFactory(
          new PropertyValueFactory<NameValuePair,String>("name")
          )
      nameCol.setMinWidth(150)
      TableColumn valueCol = new TableColumn("Value")
      valueCol.setCellValueFactory(
          new PropertyValueFactory<NameValuePair,String>("value")
          )
      valueCol.setMinWidth(400)

      Callback<TableColumn, TableCell> cellFactory =
          new Callback<TableColumn, TableCell>() {
            public TableCell call(TableColumn p) {
              return new EnvironmentEditingCell()
            }
          }

      nameCol.setCellFactory(cellFactory)
      valueCol.setCellFactory(cellFactory)
      envTable.setItems(envData)
      envTable.getColumns().addAll(nameCol, valueCol)

      envTable.setEditable(true)

      //Modifying the name property
      nameCol.setOnEditCommit(new EventHandler<CellEditEvent<NameValuePair, String>>() {
            @Override public void handle(CellEditEvent<NameValuePair, String> t) {
              ((NameValuePair)t.getTableView().getItems().get(
                  t.getTablePosition().getRow())).setName(t.getNewValue())
            }
          })

      valueCol.setOnEditCommit(new EventHandler<CellEditEvent<NameValuePair, String>>() {
            @Override public void handle(CellEditEvent<NameValuePair, String> t) {
              ((NameValuePair)t.getTableView().getItems().get(
                  t.getTablePosition().getRow())).setValue(t.getNewValue())
            }
          })

      Button addButton = new Button("Add")
      addButton.setOnAction(new ReflectiveActionEventHandler(this, "add"))

      Button removeButton = new Button("Remove")
      removeButton.setOnAction(new ReflectiveActionEventHandler(this, "remove"))
      
      configPane.add(envTable, "span, grow")
      configPane.add(addButton, "grow")
      configPane.add(removeButton, "span, grow");
    }

    return configPane
  }

  public void add(ActionEvent evt)
  { 
    envData.add(new NameValuePair("NAME", "VALUE"))
  }
  
  public void remove(ActionEvent evt)
  {
    int i = envTable.getSelectionModel().getSelectedIndex();
    
    println "REMOVING: $i"
    envData.remove(i);
    //envData.add(new NameValuePair("NAME", "VALUE"))
  }
}
