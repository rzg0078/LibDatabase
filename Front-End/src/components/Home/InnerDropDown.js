import React from "react";
import "bootstrap/dist/css/bootstrap.min.css";
import "./Home.css";
import { DropdownButton, Dropdown } from "react-bootstrap";

function InnerDropDown(props) {
  const yrs = [
    "2008",
    "2009",
    "2010",
    "2011",
    "2012",
    "2013",
    "2014",
    "2015",
    "2016",
    "2017",
    "2018",
    "2019",
    "2020",
    "2021",
  ];
  const addCategory = () => {
    props.setCategory(props.subName);
  };

  const addYear = (year) => {
    props.setYear(year);
    props.getLanguage(year);
  };

  return (
    <DropdownButton
      key="end"
      drop="end"
      variant="secondary"
      title={props.subName}
      className="d-grid gap-2"
      onClick={addCategory}
    >
      {yrs.map((y) => (
        <Dropdown.Item
          eventKey={y}
          key={y}
          onClick={(e) => {
            addYear(y);
          }}
        >
          {y}
        </Dropdown.Item>
      ))}
    </DropdownButton>
  );
}

export default InnerDropDown;
