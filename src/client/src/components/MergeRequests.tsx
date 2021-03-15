import {
  useEffect,
  useRef,
  useState,
  HTMLProps,
  MouseEventHandler,
  MouseEvent,
} from 'react'
import jsonFetcher from '../utils/jsonFetcher'
import useSuspense from '../utils/useSuspense'
import { onError } from '../utils/suspenseDefaults'
import { TCommitData, TMergeData } from '../types'
import dateConverter from '../utils/dateConverter'
import { noop } from 'lodash'
import classNames from '../utils/classNames'

import Table from '../components/Table'
import Diff from '../components/Diff'

import styles from '../css/MergeRequests.module.css'

export interface IMergeRequestsProps {
  projectId: string
  memberId?: string
}

type TTableData = {
  date: string
  title: string
  view?: JSX.Element
  score: number
  ignore: JSX.Element
}[]

const sharedTableProps = {
  sortable: true,
  collapsible: true,
  classes: {
    container: styles.tableContainer,
    table: styles.table,
    header: styles.theader,
    data: styles.tdata,
    row: styles.row,
  },
}

const eventStopper = (onClick?: (event: MouseEvent) => void) => {
  const handler: MouseEventHandler = event => {
    event.stopPropagation()
    onClick?.(event)
  }
  return handler
}

const IgnoreBox = ({
  onClick,
  className,
  ...props
}: HTMLProps<HTMLInputElement>) => (
  <input
    {...props}
    onClick={eventStopper(e => onClick?.(e as MouseEvent<HTMLInputElement>))}
    type="checkbox"
    className={classNames(styles.ignore, className)}
  />
)

const MergeRequests = ({ projectId, memberId }: IMergeRequestsProps) => {
  const [selectedMr, setSelectedMr] = useState<string>()
  const [selectedDiff, setSelectedDiff] = useState<{
    type: 'mergerequest' | 'commit'
    id: string
  }>()
  const [commits, setCommits] = useState<TCommitData>()
  const tableData = useRef<{ mrs?: TTableData; commits?: TTableData }>()

  const { Suspense, data: mergeRequests, error } = useSuspense<TMergeData>(
    (setData, setError) => {
      jsonFetcher<TMergeData>(
        `/api/project/${projectId}/members/${memberId}/mergerequests`
      )
        .then(merges => {
          const mrTableData: TTableData = []
          merges.forEach(({ time, title, mergeRequestId, score }) => {
            mrTableData.push({
              date: dateConverter(time),
              title: title,
              view: (
                <button
                  onClick={eventStopper(() => setSelectedMr(mergeRequestId))}
                  className={styles.viewBtn}
                >
                  View commits
                </button>
              ),
              score: score,
              ignore: <IgnoreBox onChange={noop} />,
            })
          })
          tableData.current = {
            ...tableData.current,
            mrs: mrTableData,
          }
          setData(merges)
        })
        .catch(onError(setError))
    }
  )

  useEffect(() => {
    if (selectedMr !== undefined) {
      jsonFetcher<TCommitData>(
        `/api/project/${projectId}/mergerequest/${selectedMr}/commits`
      )
        .then(commits => {
          const commitTableData: TTableData = []

          commits.forEach(({ score, time, title }) => {
            commitTableData.push({
              date: dateConverter(time),
              title,
              score,
              ignore: <IgnoreBox onChange={noop} />,
            })
          })

          tableData.current = {
            ...tableData.current,
            commits: commitTableData,
          }

          setCommits(commits)
        })
        .catch(console.error)
    }
  }, [selectedMr])

  const viewDiffOf = (type: 'mergerequest' | 'commit', id?: string) => {
    if (id !== undefined)
      setSelectedDiff(
        id === selectedDiff?.id && type === selectedDiff?.type
          ? undefined
          : { type, id }
      )
  }

  return (
    <Suspense
      fallback="Loading Merge Requests..."
      error={error?.message ?? 'Unknown Error'}
    >
      <div
        className={classNames(
          styles.container,
          selectedDiff !== undefined && styles.showDiff
        )}
      >
        <div className={styles.diff}>
          <Diff
            projectId={projectId}
            source={selectedDiff?.type}
            id={selectedDiff?.id}
          />
        </div>
        <div className={styles.tables}>
          <Table
            {...sharedTableProps}
            maxHeight={selectedMr === undefined ? 400 : 200}
            title="Merge Requests"
            headers={['Date', 'Title', '', 'Score', 'Ignore?']}
            columnWidths={['3fr', '6fr', '3fr', '1fr', '1fr']}
            onClick={(e, i) => {
              viewDiffOf('mergerequest', mergeRequests?.[i].mergeRequestId)
            }}
            data={tableData.current?.mrs ?? [{}]}
          />
          <Table
            {...sharedTableProps}
            isOpen={commits !== undefined}
            maxHeight={400}
            title={`Commits for MR ${selectedMr ?? ''}`}
            headers={['Date', 'Title', 'Score', 'Ignore?']}
            columnWidths={['3fr', '9fr', '1fr', '1fr']}
            onClick={(e, i) => {
              viewDiffOf('commit', commits?.[i].id)
            }}
            data={tableData.current?.commits ?? [{}]}
          />
        </div>
      </div>
    </Suspense>
  )
}

export default MergeRequests
