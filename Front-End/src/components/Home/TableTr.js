import React from "react";
import "bootstrap/dist/css/bootstrap.min.css";
import "./Home.css";

function TableTr(props) {
  return (
    <tr>
      <td>{props.json.PATRON_GROUP_NAME}</td>
      <td>{props.json.CountOfCHARGE_DATE_ONLY}</td>
      <td>{props.json.CountOfRENEWAL_COUNT}</td>
      <td>{props.json.StartDate}</td>
      <td>{props.json.EndDate}</td>
      <td>{props.json.TITLE}</td>
      <td>{props.json.CALL_NO}</td>
      <td>{props.json.AUTHOR}</td>
      <td>{props.json.PUBLISHER}</td>
      <td>{props.json.Circulation_Notes}</td>
    </tr>
  );
}

export default TableTr;
