import React, { ChangeEvent, useContext, useState } from 'react'
import {
  IUserConfig,
  SET_CONFIG,
  SET_GRAPH_BY,
  TGraphMode,
  TYAxis,
  UserConfigContext,
} from '../context/UserConfigContext'

import MuiDatePicker from '@material-ui/lab/DatePicker'
import TextField from '@material-ui/core/TextField'
import SideNavSubItem from './SideNavSubItem'
import UserConfigPopup from './UserConfigPopup'
import SavedConfigs from './SavedConfigs'
import SideNavItem from './SideNavItem'

import styles from '../css/UserConfig.module.css'

import { ReactComponent as SaveLarge } from '../assets/save-large.svg'
import { ReactComponent as SaveSmall } from '../assets/save-small.svg'
import { ReactComponent as Edit } from '../assets/edit.svg'
import { ReactComponent as toolIcon } from '../assets/tool.svg'
import { ReactComponent as settingsIcon } from '../assets/settings.svg'

const DatePicker = (props: {
  onChange: (value: Date | null) => void
  value: Date | null
  label: string
}) => (
  <MuiDatePicker
    {...props}
    renderInput={params => (
      <TextField
        {...params}
        margin="dense"
        size="small"
        helperText={null}
        className={styles.dateContainer}
      />
    )}
  />
)
const UserConfig = () => {
  const { userConfigs, dispatch } = useContext(UserConfigContext)

  const [fileScores, setFileScores] = useState(userConfigs.selected.fileScores)
  const [generalScores, setGeneralScores] = useState(
    userConfigs.selected.generalScores
  )

  const [popUpOpen, setPopUpOpen] = useState(false)

  const [name, setName] = useState(userConfigs.selected.name)

  const formatDate = (date: Date) => {
    if (date) {
      return date.toISOString().split('T')[0]
    } else {
      return
    }
  }

  const memberScoreByChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    // TODO: Add update user config post
    const newValue = event.target.value as IUserConfig['scoreBy']
    dispatch({
      type: 'SET_SCORE_BY',
      scoreBy: newValue,
    })
  }

  const graphYAxisChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    // TODO: Add update user config post
    const newValue = event.target.value as TYAxis
    dispatch({
      type: 'SET_GRAPH_Y_AXIS',
      yAxis: newValue,
    })
  }

  const projectGraphByChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    // TODO: Add update user config post
    const newValue = event.target.value as TGraphMode
    dispatch({
      type: SET_GRAPH_BY,
      graphMode: newValue,
    })
  }

  const nameChange = (event: ChangeEvent<HTMLInputElement>) => {
    const newName = event.target.value
    setName(newName)
    // if (checkUniqueName(newName)) {
    //   dispatch({ type: 'SET_CONFIG_NAME', name: newName })
    // }
  }

  const save = () => {
    // const newSavedConfigs = savedConfigs
    // newSavedConfigs.push(userConfigs.selected)
    // setSavedConfigs([...newSavedConfigs])
    setName('')
  }

  const setCurrentConfig = (newUserConfig: IUserConfig) => {
    setFileScores(newUserConfig.fileScores)
    setGeneralScores(newUserConfig.generalScores)
    if (newUserConfig.id) {
      dispatch({
        type: SET_CONFIG,
        id: newUserConfig.id,
      })
    }
  }

  const togglePopup = () => {
    setPopUpOpen(!popUpOpen)
  }

  return (
    <>
      <SideNavItem label="Settings" Icon={settingsIcon}>
        <SideNavSubItem startOpened={true} label="Date Range">
          <DatePicker
            label="Start date"
            onChange={d => d && dispatch({ type: 'SET_START_DATE', date: d })}
            value={userConfigs.selected.startDate ?? null}
          />
          <DatePicker
            label="End date"
            onChange={date => date && dispatch({ type: 'SET_END_DATE', date })}
            value={userConfigs.selected.endDate ?? null}
          />
        </SideNavSubItem>
        <SideNavSubItem startOpened={true} label="Member scores by">
          <>
            <div className={styles.inputField}>
              <input
                type="radio"
                value="Merge Requests"
                checked={userConfigs.selected.scoreBy === 'MRS'}
                onChange={memberScoreByChange}
              />
              <label className={styles.label}>Merge Requests</label>
            </div>
            <div className={styles.inputField}>
              <input
                type="radio"
                value="Commits"
                checked={userConfigs.selected.scoreBy === 'COMMITS'}
                onChange={memberScoreByChange}
              />
              <label className={styles.label}>Commits</label>
            </div>
          </>
        </SideNavSubItem>
        <SideNavSubItem startOpened={true} label="Graph Settings">
          <>
            <p className={styles.subHeader}>Graph Y-Axis</p>
            <div className={styles.inputField}>
              <input
                type="radio"
                value="NUMBER"
                checked={userConfigs.selected.yAxis === 'NUMBER'}
                onChange={graphYAxisChange}
              />
              <label className={styles.label}>Number</label>
            </div>
            <div className={styles.inputField}>
              <input
                type="radio"
                value="SCORE"
                checked={userConfigs.selected.yAxis === 'SCORE'}
                onChange={graphYAxisChange}
              />
              <label className={styles.label}>Score</label>
            </div>
            <p className={styles.subHeader}>Project Graph</p>
            <div className={styles.inputField}>
              <input
                type="radio"
                value="Entire Project"
                checked={userConfigs.selected.graphMode === 'PROJECT'}
                onChange={projectGraphByChange}
              />
              <label className={styles.label}>Entire Project</label>
            </div>
            <div className={styles.inputField}>
              <input
                type="radio"
                value="Split By Member"
                checked={userConfigs.selected.graphMode === 'MEMBER'}
                onChange={projectGraphByChange}
              />
              <label className={styles.label}>Split By Member</label>
            </div>
          </>
        </SideNavSubItem>
        <button className={styles.header} onClick={togglePopup}>
          <Edit className={styles.editIcon} /> Edit Scoring
        </button>
        {popUpOpen && (
          <UserConfigPopup
            generalScores={generalScores}
            setGeneralScores={setGeneralScores}
            fileScores={fileScores}
            setFileScores={setFileScores}
            togglePopup={togglePopup}
          />
        )}
        <div className={styles.saveContainer}>
          <div className={styles.saveLabel}>
            <SaveLarge className={styles.saveIcon} /> Save Configuration
          </div>
          <input
            value={name}
            onChange={nameChange}
            className={styles.nameInput}
            placeholder="Name config..."
          />
          <button className={styles.saveButton} onClick={save} disabled={!name}>
            <SaveSmall className={styles.saveIcon} /> Save Config
          </button>
        </div>
      </SideNavItem>
      <SideNavItem Icon={toolIcon} label="Saved Configs">
        <SavedConfigs />
      </SideNavItem>
    </>
  )
}

export default UserConfig
