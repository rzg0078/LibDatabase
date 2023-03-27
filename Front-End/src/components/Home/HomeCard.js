import React, { useState } from "react";
import "bootstrap/dist/css/bootstrap.min.css";
import "./Home.css";
import { Card, Table, Form } from "react-bootstrap";
import DropDown from "./DropDown.js";
import TableTr from "./TableTr.js";

function HomeCard() {
  const [semester, setSemester] = useState("");
  const [category, setCategory] = useState("");
  const [year, setYear] = useState("");
  const [fileResponse, setFileResponse] = useState("");
  const [json, setJson] = useState(0);
  const header = [
    "Patron Group Name",
    "Count Of Charge Date Only",
    "Count Of Renewal Count",
    "Start Date",
    "End Date",
    "Title",
    "Call No",
    "Author",
    "Publisher",
    "Circulation Notes",
  ];

  const getLanguage = (year) => {
    const getAllData = {
      semester,
      subParts: category,
      year,
    };
    fetch("http://localhost:8080/getData", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(getAllData),
    })
      .then((response) => response.json())
      .then((responseJson) => {
        setJson(responseJson);
        setSemester("");
        setCategory("");
        setYear("");
      })
      .catch();
  };

  const uploadFile = (file) => {
    const formData = new FormData();
    formData.append("file", file);
    fetch("http://localhost:8080/uploadData", {
      method: "POST",
      body: formData,
    })
      .then((response) => response.text())
      .then((responseJson) => {
        setFileResponse(responseJson);
      })
      .catch();
  };

  return (
    <div>
      <Card className="text-center card-width marg-top">
        <Card.Body>
          <div className="top-padding"></div>
          <div className="row">
            <div className="col-6 shift-left divs">
              <DropDown
                name="Fall Semester"
                shortName="Fall"
                setSemester={setSemester}
                setCategory={setCategory}
                setYear={setYear}
                year={year}
                getLanguage={getLanguage}
              />
            </div>
            <div className="col-6 shift-right">
              <Form.Group controlId="formFile" className="mb-3">
                <Form.Control
                  type="file"
                  className="form-btn-main inline-button"
                  onInput={(e) => {
                    uploadFile(e.target.files[0]);
                  }}
                />
              </Form.Group>
              <p className="p-file">{fileResponse}</p>
            </div>
          </div>
          <div className="row">
            <div className="col-6 shift-left divs">
              <DropDown
                name="Spring Semester"
                shortName="Spring"
                setSemester={setSemester}
                setCategory={setCategory}
                setYear={setYear}
                year={year}
                getLanguage={getLanguage}
              />
            </div>
          </div>
          <div className="row">
            <div className="col-6 shift-left divs">
              <DropDown
                name="Summer Semester"
                shortName="Summer"
                setSemester={setSemester}
                setCategory={setCategory}
                setYear={setYear}
                year={year}
                getLanguage={getLanguage}
              />
            </div>
          </div>
          <div className="padding-main"></div>
          <div className="table-change">
            <Table striped bordered responsive>
              <thead>
                <tr>
                  {header.map((head) => (
                    <th key={head}>{head}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {json !== 0 &&
                  json.map((js, i) => <TableTr key={i} json={js} />)}
              </tbody>
            </Table>
          </div>
        </Card.Body>
      </Card>
    </div>
  );
}

export default HomeCard;
