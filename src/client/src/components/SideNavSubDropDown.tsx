import { useState, ReactNode } from 'react'

import styles from '../css/SideNavSubDropDown.module.css'

import { ReactComponent as Dropdown } from '../assets/dropdown-small.svg'

export interface ISideNavSubDropDown {
  children: ReactNode
  startOpened: boolean
  label: string
}
const SideNavSubDropDown = ({
  children,
  startOpened,
  label,
}: ISideNavSubDropDown) => {
  const [isOpen, setIsOpen] = useState(startOpened)

  const toggleTab = () => {
    setIsOpen(!isOpen)
  }

  return (
    <div>
      <button className={styles.header} onClick={toggleTab}>
        {label}
        <Dropdown className={isOpen ? styles.openIcon : styles.closedIcon} />
      </button>
      {isOpen && <div className={styles.subContainer}>{children}</div>}
    </div>
  )
}

export default SideNavSubDropDown
