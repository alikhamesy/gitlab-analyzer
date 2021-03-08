import classNames from '../utils/classNames'

import SideNavItem from './SideNavItem'

import styles from '../css/SideNav.module.css'

import { ReactComponent as repoIcon } from '../assets/database.svg'
import { ReactComponent as reportIcon } from '../assets/report.svg'
import { ReactComponent as settingsIcon } from '../assets/settings.svg'

export interface ISideNavProps {
  isOpen: boolean
  sideNavToggler: () => void
  className?: string
}

const items = [
  { icon: repoIcon, label: 'Projects', dest: '/home' },
  { icon: reportIcon, label: 'Reports', dest: '/reports' },
  { icon: settingsIcon, label: 'Settings', dest: '/settings' },
]

const SideNav = ({ isOpen, sideNavToggler, className }: ISideNavProps) => {
  const toggleSideNav = () => {
    sideNavToggler()
  }

  return (
    <aside className={classNames(styles.sideNav, className)}>
      <button
        type="button"
        className={styles.closeSideNavButton}
        onClick={toggleSideNav}
      >
        {isOpen ? <>&#8249;</> : <>&#8250;</>}
      </button>
      {items.map(item => (
        <SideNavItem
          key={item.label}
          Icon={item.icon}
          label={item.label}
          destPath={item.dest}
        />
      ))}
    </aside>
  )
}

export default SideNav
