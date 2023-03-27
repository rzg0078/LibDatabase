import React from "react";
import "bootstrap/dist/css/bootstrap.min.css";
import "./Home.css";
import { DropdownButton } from "react-bootstrap";
import InnerDropDown from "./InnerDropDown.js";

function DropDown(props) {
  const addSemester = () => {
    props.setSemester(props.shortName);
  };
  return (
    <DropdownButton
      id="dropdown-basic-button"
      title={props.name}
      className="drop-main inline-button"
      onClick={addSemester}
    >
      <InnerDropDown
        subName="Microfilm"
        setCategory={props.setCategory}
        setYear={props.setYear}
        getLanguage={props.getLanguage}
      />
      <InnerDropDown
        subName="Miscellaneous"
        setCategory={props.setCategory}
        setYear={props.setYear}
        getLanguage={props.getLanguage}
      />
      <InnerDropDown
        subName="Traditional"
        setCategory={props.setCategory}
        setYear={props.setYear}
        getLanguage={props.getLanguage}
      />
      <InnerDropDown
        subName="No Call"
        setCategory={props.setCategory}
        setYear={props.setYear}
        getLanguage={props.getLanguage}
      />
    </DropdownButton>
  );
}

export default DropDown;
