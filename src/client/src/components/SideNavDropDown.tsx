import { useState } from 'react'

import styles from '../css/SideNavDropDown.module.css'

export interface ISideNavDropDown {
  children: JSX.Element
  Icon: React.FunctionComponent<React.SVGProps<SVGSVGElement>>
  label: string
}

const SideNavDropDown = ({ children, Icon, label }: ISideNavDropDown) => {
  const [open, setOpen] = useState(false)

  const toggleTab = () => {
    setOpen(!open)
  }

  return (
    <>
      <div className={styles.item} onClick={toggleTab}>
        <Icon className={styles.icon} />
        <p className={styles.label}>{label}</p>
        {open && <span>V</span>}
        {!open && <span>{'>'}</span>}
      </div>
      <div className={styles.container}>{open && children}</div>
    </>
  )
}

export default SideNavDropDown
