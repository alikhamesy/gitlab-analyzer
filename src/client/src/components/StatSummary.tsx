import { useState, useEffect, useRef } from 'react'
import { ThemeProvider, Tooltip } from '@material-ui/core'
import Stat, { IStatProps } from './Stat'
import tooltipTheme from '../themes/tooltipTheme'

import styles from '../css/StatSummary.module.css'

import clipboard from '../assets/clipboard.svg'

export interface IStatSummaryProps {
  statData: IStatProps[]
}

const StatSummary = ({ statData }: IStatSummaryProps) => {
  const [copyMessage, setCopyMessage] = useState('Copy stats')
  const [csvString, setCsvString] = useState('')
  const timeoutRef = useRef<NodeJS.Timeout>()
  const copyNodeRef = useRef<HTMLTextAreaElement>(null)

  const clipboardCopyFallback = () => {
    if (!copyNodeRef.current) {
      setCopyMessage('Failed to copy')
      timeoutRef.current = setTimeout(() => setCopyMessage('Copy stats'), 5000)
      return
    }

    copyNodeRef.current.value = csvString
    copyNodeRef.current.select()
    document.execCommand('copy')
    setCopyMessage('Copied!')
    timeoutRef.current = setTimeout(() => setCopyMessage('Copy stats'), 5000)
  }

  useEffect(() => {
    setCsvString(
      [
        ['Stat', 'Value'],
        ...statData.map(stat => [stat.name, stat.rawValue ?? stat.value]),
      ]
        .map(row => row.join('\t'))
        .join('\n')
    )
  }, [statData])

  const copyToClipboard = () => {
    //TODO: Implement navigator.permissions query for clipboard-read/write
    navigator.clipboard
      ?.writeText(csvString)
      .then(() => {
        setCopyMessage('Copied!')
        timeoutRef.current = setTimeout(
          () => setCopyMessage('Copy stats'),
          5000
        )
      })
      .catch(() => {
        clipboardCopyFallback()
      }) ?? clipboardCopyFallback()
  }

  useEffect(() => {
    return () => timeoutRef.current && clearTimeout(timeoutRef.current)
  }, [])

  return (
    <ThemeProvider theme={tooltipTheme}>
      <div className={styles.container}>
        {statData?.map(stat => (
          <Stat key={stat.name} {...stat} />
        ))}
        <div className={styles.statTools}>
          <Tooltip title={copyMessage} arrow>
            <button onClick={copyToClipboard} className={styles.copyButton}>
              <img src={clipboard} className={styles.copyIcon} />
            </button>
          </Tooltip>
        </div>
        <textarea
          style={{ position: 'fixed', left: '-10000000px' }}
          ref={copyNodeRef}
        />
      </div>
    </ThemeProvider>
  )
}

export default StatSummary
